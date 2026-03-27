package org.oredredging.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.oredredging.registry.ModEnchantments;
import org.oredredging.registry.ModSoundEvent;
import org.oredredging.util.RandomUtil;

import java.util.*;

/**
 * 崩石锤 - 可通过蓄力破坏方块，支持沉重附魔增强蓄力和连锁破坏
 */
public class CollapseStoneHammerItem extends SwordItem implements CrushedDropGain, Wave, PossibleEnchantment {

    // ==================== 基础配置常量 ====================
    private static final float CHARGE_PER_TICK = 0.05F;          // 每刻基础能量积累
    private static final double MAX_RAYCAST_DISTANCE = 7.0D;     // 最大射线检测距离
    private static final int BASE_MAX_USE_TIME = 70000;          // 基础最大蓄力时间（刻）
    private static final int COOLDOWN_TICKS = 10;                // 使用后冷却刻数
    private static final float BREAK_PROGRESS_STEPS = 9.0F;      // 破坏进度条最大分段数

    // ==================== 沉重附魔相关常量 ====================
    private static final int EXTRA_TICKS_PER_HEAVY_LEVEL = 6;            // 每级沉重附魔增加的蓄力时间
    private static final float ENERGY_MULTIPLIER_PER_LEVEL = 0.25F;      // 每级沉重附魔能量倍率增量
    private static final int CHAIN_TRIGGER_THRESHOLD = 20;           // 触发连锁破坏所需蓄力时间

    // 连锁破坏基础参数（随等级线性增长，可单独调整）
    private static final float BASE_CHAIN_PROBABILITY = 0.3F;             // 基础连锁概率
    private static final float PROBABILITY_PER_LEVEL = 0.2F;              // 每级增加的概率
    private static final float MAX_CHAIN_PROBABILITY = 0.8F;              // 最大连锁概率
    private static final int BASE_MAX_CHAIN_COUNT = 0;                    // 基础最大连锁数量
    private static final int MAX_COUNT_PER_LEVEL = 3;                     // 每级增加的最大连锁数量

    // 用于属性修饰符的唯一 ID
    private static final UUID HEAVY_DAMAGE_UUID = UUID.fromString("b1c2d3e4-f506-0708-0900-010203040506");
    private static final UUID HEAVY_SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-0708-0900-010203040506");

    // ==================== 构造方法 ====================
    public CollapseStoneHammerItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    // ==================== 核心方法：蓄力释放 ====================
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        BlockHitResult hitResult = raycast(world, user);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            clearTargetAndProgress(stack, world, player);
            return;
        }

        BlockPos targetPos = hitResult.getBlockPos();
        BlockState targetState = world.getBlockState(targetPos);
        float hardness = targetState.getHardness(world, targetPos);

        if (isUnbreakable(hardness, targetState)) {
            clearTargetAndProgress(stack, world, player);
            return;
        }

        // 获取NBT中存储的旧目标与累计能量
        NbtCompound nbt = stack.getOrCreateNbt();
        BlockPos storedTarget = getStoredTarget(nbt);
        float accumulatedEnergy = getStoredEnergy(nbt);

        // 检查目标是否变更
        boolean targetChanged = !targetPos.equals(storedTarget);
        if (targetChanged) {
            // 清除旧目标的进度条
            if (storedTarget != null) {
                world.setBlockBreakingInfo(player.getId(), storedTarget, -1);
            }
            accumulatedEnergy = 0.0F;
            setTarget(nbt, targetPos);
        }

        // 获取沉重附魔等级
        int heavyLevel = getHeavyLevel(stack);
        int maxUseTime = getMaxUseTime(stack);
        int usedTicks = maxUseTime - remainingUseTicks;
        float addedEnergy = calculateAddedEnergy(usedTicks, heavyLevel);
        accumulatedEnergy += addedEnergy;

        // 检查是否足以破坏方块
        if (accumulatedEnergy >= hardness) {
            // 破坏主目标方块
            breakBlock(world, player, targetPos, targetState, stack);

            // 生成砸碎方块特效
            spawnBreakingRingEffect(world, targetPos, targetState);

            // 尝试触发连锁破坏（仅在附魔等级>0且蓄力超过阈值时）
            boolean shouldChain = heavyLevel > 0 && usedTicks > CHAIN_TRIGGER_THRESHOLD;
            if (shouldChain) {
                performChainDestruction(world, player, targetPos, targetState.getBlock(), stack, heavyLevel);
            }

            clearTargetAndProgress(stack, world, player);
        } else {
            // 未破坏：保存能量，更新进度条
            nbt.putFloat("Energy", accumulatedEnergy);
            updateBreakingProgress(world, player, targetPos, targetState, accumulatedEnergy, hardness);
            playHitEffects(world, player, targetPos, targetState);
        }

        // 通用结束操作
        player.swingHand(Hand.MAIN_HAND);
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        world.playSound(player, targetPos, ModSoundEvent.HAMMER_HIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    // ==================== 使用与最大蓄力时间 ====================
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        int heavyLevel = getHeavyLevel(stack);
        return BASE_MAX_USE_TIME + heavyLevel * EXTRA_TICKS_PER_HEAVY_LEVEL;
    }

    @Override
    public int getProbability(int original) {
        return (int) (original * 0.38);
    }

    // ==================== 辅助方法 ====================
    private BlockHitResult raycast(World world, LivingEntity user) {
        Vec3d eyePos = user.getEyePos();
        Vec3d lookVec = user.getRotationVec(1.0F);
        Vec3d endPos = eyePos.add(lookVec.multiply(MAX_RAYCAST_DISTANCE));
        RaycastContext context = new RaycastContext(
                eyePos,
                endPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                user
        );
        return world.raycast(context);
    }

    private int getHeavyLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(ModEnchantments.HEAVY, stack);
    }

    private float calculateAddedEnergy(int usedTicks, int heavyLevel) {
        float multiplier = 1.0F + heavyLevel * ENERGY_MULTIPLIER_PER_LEVEL;
        return usedTicks * CHARGE_PER_TICK * multiplier;
    }

    private BlockPos getStoredTarget(NbtCompound nbt) {
        if (nbt.contains("TargetX")) {
            int x = nbt.getInt("TargetX");
            int y = nbt.getInt("TargetY");
            int z = nbt.getInt("TargetZ");
            return new BlockPos(x, y, z);
        }
        return null;
    }

    private float getStoredEnergy(NbtCompound nbt) {
        return nbt.contains("Energy") ? nbt.getFloat("Energy") : 0.0F;
    }

    private void setTarget(NbtCompound nbt, BlockPos pos) {
        nbt.putInt("TargetX", pos.getX());
        nbt.putInt("TargetY", pos.getY());
        nbt.putInt("TargetZ", pos.getZ());
    }

    private void clearTargetAndProgress(ItemStack stack, World world, PlayerEntity player) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            BlockPos oldPos = getStoredTarget(nbt);
            if (oldPos != null) {
                world.setBlockBreakingInfo(player.getId(), oldPos, -1);
            }
            nbt.remove("TargetX");
            nbt.remove("TargetY");
            nbt.remove("TargetZ");
            nbt.remove("Energy");
        }
    }

    private boolean isUnbreakable(float hardness, BlockState state) {
        return hardness < 0.0F || state.isAir();
    }

    /**
     * 破坏方块并播放特效
     */
    private void breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, ItemStack hammer) {
        world.breakBlock(pos, false, player);
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity, player, hammer);
        hammer.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
    }

    /**
     * 生成由内向外扩散的方块粒子圆环（砸碎特效）
     * @param world 世界
     * @param pos 方块位置
     * @param state 方块状态，用于粒子材质
     */
    private void spawnBreakingRingEffect(World world, BlockPos pos, BlockState state) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        int particleCount = 48;
        BlockStateParticleEffect particle = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);

        for (int i = 0; i < particleCount; i++) {
            // 基础角度
            double angle = 2 * Math.PI * i / particleCount;
            // 半径随机偏移
            double radius = 1.2 + (world.random.nextDouble() - 0.5) * 0.8;
            // 垂直偏移随机
            double yOffset = (world.random.nextDouble() - 0.5) * 0.8;

            double xOffset =  Math.cos(angle);
            double zOffset =  Math.sin(angle);
            double px = centerX + xOffset;
            double py = centerY;
            double pz = centerZ + zOffset;

            // 速度方向：径向向外，速度较快（1.2 ~ 2.2）
            double dx = xOffset;
            double dy = yOffset;
            double dz = zOffset;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0.01) {
                dx /= len;
                dy /= len;
                dz /= len;
            }
            double speed = 1.2 + world.random.nextDouble();
            double vx = dx * speed;
            double vy = dy * speed;
            double vz = dz * speed;

            world.addParticle(particle, px, py, pz, vx, vy, vz);
        }
    }

    private void updateBreakingProgress(World world, PlayerEntity player, BlockPos pos, BlockState state, float energy, float hardness) {
        int progress = (int) ((energy / hardness) * BREAK_PROGRESS_STEPS);
        world.setBlockBreakingInfo(player.getId(), pos, progress);
    }

    private void playHitEffects(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        // 粒子效果
        for (int i = 0; i < 10; i++) {
            world.addParticle(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    RandomUtil.nextInt(7) - 3, RandomUtil.nextInt(7) - 3, RandomUtil.nextInt(7) - 3
            );
        }
        // 声音效果
        world.playSound(player, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 0.8F, 1.0F);
    }

    // ==================== 连锁破坏逻辑 ====================
    private void performChainDestruction(World world, PlayerEntity player, BlockPos centerPos, Block targetBlock, ItemStack hammer, int heavyLevel) {
        // 根据附魔等级计算连锁参数
        float probability = Math.min(BASE_CHAIN_PROBABILITY + heavyLevel * PROBABILITY_PER_LEVEL, MAX_CHAIN_PROBABILITY);
        int maxCount = BASE_MAX_CHAIN_COUNT + heavyLevel * MAX_COUNT_PER_LEVEL;

        if (world.isClient()) return;

        Set<BlockPos> visited = new HashSet<>();          // 所有已处理过的位置（包括被破坏和未被破坏的）
        List<BlockPos> toDestroy = new ArrayList<>();     // 最终待破坏的方块列表
        Deque<BlockPos> currentLayer = new ArrayDeque<>(); // 当前层的候选方块

        // 初始化：中心相邻方块作为第一层候选
        visited.add(centerPos);
        for (BlockPos adjacent : getAdjacentPositions(centerPos)) {
            if (!visited.contains(adjacent)) {
                currentLayer.add(adjacent);
            }
        }

        int destroyed = 0;

        // 逐层扩散，最多 heavyLevel 层
        for (int layer = 1; layer <= heavyLevel; layer++) {
            if (currentLayer.isEmpty()) break;

            // 将当前层随机排序，增加随机性
            List<BlockPos> layerList = new ArrayList<>(currentLayer);
            shuffleList(layerList, world.random);
            currentLayer.clear();

            Deque<BlockPos> nextLayer = new ArrayDeque<>();

            for (BlockPos pos : layerList) {
                if (destroyed >= maxCount) break;   // 达到数量上限，停止扩散

                if (visited.contains(pos)) continue;
                visited.add(pos);

                if (world.getBlockState(pos).getBlock() == targetBlock) {
                    // 概率判定是否破坏
                    if (RandomUtil.randomBoolean(probability)) {
                        toDestroy.add(pos);
                        destroyed++;

                        // 将该方块的相邻未访问方块加入下一层候选
                        for (BlockPos neighbor : getAdjacentPositions(pos)) {
                            if (!visited.contains(neighbor)) {
                                nextLayer.add(neighbor);
                            }
                        }
                    }
                }
            }

            // 准备下一层
            currentLayer = nextLayer;
        }

        // 执行破坏
        for (BlockPos pos : toDestroy) {
            BlockState state = world.getBlockState(pos);
            breakBlock(world, player, pos, state, hammer);

            // 连锁特效（额外加一点小粒子，让连锁更明显）
            world.addParticle(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    RandomUtil.nextInt(5) - 2, RandomUtil.nextInt(5) - 2, RandomUtil.nextInt(5) - 2
            );
            world.playSound(player, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 0.6F, 1.2F);
        }
    }

    /**
     * 获取方块六个面的相邻位置（北、南、西、东、下、上）
     */
    private List<BlockPos> getAdjacentPositions(BlockPos pos) {
        return List.of(
                pos.north(), pos.south(), pos.west(), pos.east(),
                pos.down(), pos.up()
        );
    }

    /**
     * Fisher-Yates 洗牌算法随机打乱列表
     */
    private void shuffleList(List<?> list, net.minecraft.util.math.random.Random random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(list, i, j);
        }
    }

    // ==================== 动态属性修饰符 ====================
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> original = super.getAttributeModifiers(stack, slot);
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = HashMultimap.create(original);

        if (slot == EquipmentSlot.MAINHAND) {
            int heavyLevel = getHeavyLevel(stack);
            if (heavyLevel > 0) {
                // 增加攻击伤害
                modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(HEAVY_DAMAGE_UUID, "Heavy enchantment damage",
                                2.0 * heavyLevel, EntityAttributeModifier.Operation.ADDITION));

                // 减缓攻击速度
                modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(HEAVY_SPEED_UUID, "Heavy enchantment speed",
                                -0.025, EntityAttributeModifier.Operation.ADDITION));
            }
        }
        return modifiers;
    }

    @Override
    public List<EnchantmentLevelEntry> modifyList(List<EnchantmentLevelEntry> original, int power, ItemStack stack, boolean treasureAllowed) {
        List<EnchantmentLevelEntry> result = new ArrayList<>(original);
        EnchantmentLevelEntry entry = PossibleEnchantment.getBestLevelEntry(ModEnchantments.HEAVY, power);

        if (entry != null) {
            result.add(entry);
        }

        return result;
    }
}