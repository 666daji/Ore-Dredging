package org.oredredging.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.oredredging.registry.ModEnchantments;
import org.oredredging.registry.ModSoundEvent;
import org.oredredging.util.RandomUtil;

import java.util.*;

public class CollapseStoneHammerItem extends SwordItem implements CrushedDropGain, Wave, PossibleEnchantment {
    private static final float CHARGE_PER_TICK = 0.05F;          // 每刻基础能量积累
    private static final int BASE_MAX_USE_TIME = 70000;          // 基础最大蓄力时间（刻）
    private static final int COOLDOWN_TICKS = 10;                // 使用后冷却刻数
    private static final float BREAK_PROGRESS_STEPS = 9.0F;      // 破坏进度条最大分段数

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

    private static final String LAST_DISPLAYED_POS = "LastDisplayedPos";

    public CollapseStoneHammerItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        if (!world.isClient) {
            HitResult hitResult = ProjectileUtil.getCollision(user, entity -> !entity.isSpectator() && entity.canHit(), 15);
            int heavyLevel = getHeavyLevel(stack);
            int usedTicks = getMaxUseTime(stack) - remainingUseTicks;
            float addedEnergy = calculateAddedEnergy(usedTicks, heavyLevel);

            // 命中实体
            if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult entityHit) {
                clearTargetData(stack);
                entityHit.getEntity().damage(world.getDamageSources().create(DamageTypes.FALLING_ANVIL, user), addedEnergy * 10);
            }
            // 命中方块
            else if (hitResult instanceof BlockHitResult blockHit) {
                BlockPos targetPos = blockHit.getBlockPos();
                BlockState targetState = world.getBlockState(targetPos);
                float hardness = targetState.getHardness(world, targetPos);

                if (hardness < 0.0F || targetState.isAir()) {
                    // 不可破坏方块：清除进度
                    clearTargetData(stack);
                } else {
                    NbtCompound nbt = stack.getOrCreateNbt();
                    BlockPos storedTarget = getStoredTarget(nbt);
                    float accumulatedEnergy = getStoredEnergy(nbt);

                    // 目标变更：重置能量并更新 NBT 目标
                    if (!targetPos.equals(storedTarget)) {
                        accumulatedEnergy = 0.0F;
                        setTarget(nbt, targetPos);
                    }

                    accumulatedEnergy += addedEnergy;
                    nbt.putFloat("Energy", accumulatedEnergy); // 始终更新能量值

                    if (accumulatedEnergy >= hardness) {
                        // 破坏成功：执行破坏、特效、连锁，然后清除 NBT
                        breakBlock(world, player, targetPos, targetState, stack);
                        spawnBreakingRingEffect(world, targetPos, targetState);
                        if (heavyLevel > 0 && usedTicks > CHAIN_TRIGGER_THRESHOLD) {
                            performChainDestruction(world, player, targetPos, targetState.getBlock(), stack, heavyLevel);
                        }
                        clearTargetData(stack); // 清除 NBT，客户端 inventoryTick 将移除进度显示
                    }
                }
            }
        }

        stack.getOrCreateNbt().putBoolean("ProgressUpdate", true);
        player.swingHand(Hand.MAIN_HAND);
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        world.playSound(player, player.getBlockPos(), ModSoundEvent.HAMMER_HIT, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient || !(entity instanceof PlayerEntity player)) return;

        // 取消选中时，清除残留进度显示
        if (!selected) {
            if (stack.getNbt() != null && stack.hasNbt() && stack.getNbt().contains(LAST_DISPLAYED_POS)) {
                BlockPos lastPos = readLastDisplayedPos(stack.getNbt());
                if (lastPos != null) {
                    world.setBlockBreakingInfo(player.getId(), lastPos, -1);
                    stack.getNbt().remove(LAST_DISPLAYED_POS);
                }
            }
            return;
        }

        // 没有更新标记则直接跳过
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.getBoolean("ProgressUpdate")) return;

        // 消费标记
        nbt.putBoolean("ProgressUpdate", false);

        // 读取当前蓄力目标与能量
        BlockPos target = null;
        float energy = 0;
        float hardness = -1;
        if (nbt.contains("TargetX")) {
            target = new BlockPos(nbt.getInt("TargetX"), nbt.getInt("TargetY"), nbt.getInt("TargetZ"));
            energy = nbt.getFloat("Energy");
            BlockState state = world.getBlockState(target);
            hardness = state.getHardness(world, target);
        }

        // 清除旧位置裂纹
        BlockPos lastPos = readLastDisplayedPos(nbt);
        if (lastPos != null && !lastPos.equals(target)) {
            world.setBlockBreakingInfo(player.getId(), lastPos, -1);
        }

        // 更新当前目标裂纹
        if (target != null && hardness >= 0 && energy > 0) {
            int progress = (int) ((energy / hardness) * BREAK_PROGRESS_STEPS);
            world.setBlockBreakingInfo(player.getId(), target, progress);
            writeLastDisplayedPos(nbt, target);
        } else {
            // 无效目标，清除所有显示
            if (lastPos != null) {
                world.setBlockBreakingInfo(player.getId(), lastPos, -1);
            }
            nbt.remove(LAST_DISPLAYED_POS);
        }
    }

    private BlockPos readLastDisplayedPos(NbtCompound nbt) {
        if (nbt.contains(LAST_DISPLAYED_POS)) {
            NbtCompound posTag = nbt.getCompound(LAST_DISPLAYED_POS);
            return new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
        }
        return null;
    }

    private void writeLastDisplayedPos(NbtCompound nbt, BlockPos pos) {
        NbtCompound posTag = new NbtCompound();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        nbt.put(LAST_DISPLAYED_POS, posTag);
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

    // ==================== 附魔与能量计算 ====================
    private int getHeavyLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(ModEnchantments.HEAVY, stack);
    }

    /**
     * 根据蓄力刻数和沉重附魔等级计算本次增加的破坏能量。
     * 能量 = 蓄力刻数 * 基础倍率 * 附魔倍率
     */
    private float calculateAddedEnergy(int usedTicks, int heavyLevel) {
        float multiplier = 1.0F + heavyLevel * ENERGY_MULTIPLIER_PER_LEVEL;
        return usedTicks * CHARGE_PER_TICK * multiplier;
    }

    // ==================== NBT 存储操作 ====================
    private BlockPos getStoredTarget(NbtCompound nbt) {
        if (nbt.contains("TargetX")) {
            return new BlockPos(nbt.getInt("TargetX"), nbt.getInt("TargetY"), nbt.getInt("TargetZ"));
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

    /**
     * 清除 NBT 中的蓄力目标与能量（服务端调用，客户端通过 inventoryTick 同步清理显示）
     */
    private void clearTargetData(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            nbt.remove("TargetX");
            nbt.remove("TargetY");
            nbt.remove("TargetZ");
            nbt.remove("Energy");
            nbt.remove(LAST_DISPLAYED_POS);
        }
    }

    // ==================== 破坏与特效 ====================
    /**
     * 破坏一个方块，处理掉落并消耗锤子耐久。
     */
    private void breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, ItemStack hammer) {
        world.breakBlock(pos, false, player);
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity, player, hammer);
        hammer.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
    }

    /**
     * 生成由内向外扩散的方块粒子圆环（砸碎特效）。
     */
    private void spawnBreakingRingEffect(World world, BlockPos pos, BlockState state) {
        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        int particleCount = 48;
        BlockStateParticleEffect particle = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);

        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double radius = 1.2 + (world.random.nextDouble() - 0.5) * 0.8;
            double yOffset = (world.random.nextDouble() - 0.5) * 0.8;

            double xOffset = Math.cos(angle);
            double zOffset = Math.sin(angle);
            double px = centerX + xOffset;
            double py = centerY;
            double pz = centerZ + zOffset;

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

    /**
     * 更新服务器显示的方块破坏进度。
     */
    private void updateBreakingProgress(World world, PlayerEntity player, BlockPos pos, BlockState state, float energy, float hardness) {
        int progress = (int) ((energy / hardness) * BREAK_PROGRESS_STEPS);
        world.setBlockBreakingInfo(player.getId(), pos, progress);
    }

    /**
     * 播放打击方块的粒子和声音效果（用于未完全破坏时）。
     */
    private void playHitEffects(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        for (int i = 0; i < 10; i++) {
            world.addParticle(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    RandomUtil.nextInt(7) - 3, RandomUtil.nextInt(7) - 3, RandomUtil.nextInt(7) - 3
            );
        }
        world.playSound(player, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 0.8F, 1.0F);
    }

    // ==================== 连锁破坏逻辑 ====================
    /**
     * 执行连锁破坏：从中心方块开始，逐层向相邻方块扩散，破坏同种方块。
     * 扩散层数等于沉重附魔等级，每层每个方块有概率被破坏，且总数受上限约束。
     *
     * @param world       世界对象
     * @param player      玩家
     * @param centerPos   被破坏的中心方块坐标
     * @param targetBlock 目标方块种类
     * @param hammer      使用的锤子物品
     * @param heavyLevel  沉重附魔等级
     */
    private void performChainDestruction(World world, PlayerEntity player, BlockPos centerPos, Block targetBlock, ItemStack hammer, int heavyLevel) {
        float probability = Math.min(BASE_CHAIN_PROBABILITY + heavyLevel * PROBABILITY_PER_LEVEL, MAX_CHAIN_PROBABILITY);
        int maxCount = BASE_MAX_CHAIN_COUNT + heavyLevel * MAX_COUNT_PER_LEVEL;
        if (world.isClient()) return; // 连锁仅在服务端执行

        Set<BlockPos> visited = new HashSet<>();          // 已处理坐标，避免重复
        List<BlockPos> toDestroy = new ArrayList<>();     // 待破坏方块
        Deque<BlockPos> currentLayer = new ArrayDeque<>(); // 当前层候选

        // 第一层：中心方块的六个相邻方块
        visited.add(centerPos);
        for (BlockPos adjacent : getAdjacentPositions(centerPos)) {
            if (!visited.contains(adjacent)) {
                currentLayer.add(adjacent);
            }
        }

        int destroyed = 0;

        // 逐层扩散，最多为附魔等级层
        for (int layer = 1; layer <= heavyLevel; layer++) {
            if (currentLayer.isEmpty()) break;

            // 随机打乱当前层顺序，使破坏更自然
            List<BlockPos> layerList = new ArrayList<>(currentLayer);
            shuffleList(layerList, world.random);
            currentLayer.clear();

            Deque<BlockPos> nextLayer = new ArrayDeque<>();

            for (BlockPos pos : layerList) {
                if (destroyed >= maxCount) break;
                if (visited.contains(pos)) continue;
                visited.add(pos);

                if (world.getBlockState(pos).getBlock() == targetBlock) {
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
            currentLayer = nextLayer;
        }

        // 统一执行破坏与特效
        for (BlockPos pos : toDestroy) {
            BlockState state = world.getBlockState(pos);
            breakBlock(world, player, pos, state, hammer);
            world.addParticle(
                    new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    RandomUtil.nextInt(5) - 2, RandomUtil.nextInt(5) - 2, RandomUtil.nextInt(5) - 2
            );
            world.playSound(player, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 0.6F, 1.2F);
        }
    }

    /**
     * 获取某个方块六个面的相邻坐标（北、南、西、东、下、上）。
     */
    private List<BlockPos> getAdjacentPositions(BlockPos pos) {
        return List.of(
                pos.north(), pos.south(), pos.west(), pos.east(),
                pos.down(), pos.up()
        );
    }

    /**
     * 随机打乱列表元素。
     */
    private void shuffleList(List<?> list, Random random) {
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