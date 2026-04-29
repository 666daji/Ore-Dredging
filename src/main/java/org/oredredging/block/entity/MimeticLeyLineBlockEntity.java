package org.oredredging.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.oredredging.OreDredging;
import org.oredredging.block.MimeticLeyLineBlock;
import org.oredredging.config.LootPoolConfig;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;
import org.oredredging.registry.ModBlockEntities;
import org.oredredging.registry.ModParticleTypes;
import org.oredredging.registry.ModSoundEvent;
import org.oredredging.util.EnhancedAnimationState;
import org.oredredging.util.RandomUtil;

import java.util.List;

public class MimeticLeyLineBlockEntity extends BlockEntity {
    /** 喷涌阶段固定时长（ticks），12秒 = 240 ticks */
    private static final int ERUPT_DURATION = 240;
    /** 喷涌开始后多久喷出物品（ticks），5秒 = 100 ticks */
    private static final int ERUPT_SPAWN_OFFSET = 110;
    /** 最大单次进度补偿（ticks），防止单次 delta 过大导致性能问题或溢出 */
    private static final long MAX_DELTA = 24000L * 30; // 30天
    /** 抽奖最大尝试次数，防止极端概率下无限循环 */
    private static final int MAX_SELECT_ATTEMPTS = 100;

    /** 当前周期已累计的进度（ticks） */
    private long progress;
    /** 上次 tick 时的世界总时间（用于计算 delta） */
    private long lastWorldTime;
    /** 当前周期选中的抽奖项 */
    private LootPoolConfig.PoolEntry currentEntry;
    /** 本次喷涌是否已生成物品（防止重复生成） */
    private boolean hasErupted;
    /** 萌发阶段粒子特效的剩余持续时间 **/
    protected int buddingParticleTime;
    /** 当前运行状态 */
    private State state = State.AMASS;

    public final EnhancedAnimationState amassAnimationState = new EnhancedAnimationState();

    public final EnhancedAnimationState buddingAnimationState = new EnhancedAnimationState();

    public final EnhancedAnimationState eruptAnimationState = new EnhancedAnimationState();

    public MimeticLeyLineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIMETIC_LEY_LINE, pos, state);
    }

    // ======================== 核心 tick 逻辑 ========================

    /**
     * 服务端每 tick 调用，根据真实时间差更新进度。
     */
    public static void tick(World world, BlockPos pos, BlockState state, MimeticLeyLineBlockEntity entity) {
        randomParticle(world, pos, entity);
        playSound(world, entity, pos);

        if (world.isClient()) {
            entity.processAnimation();
            return;
        }

        // 初始化世界时间基准
        if (entity.lastWorldTime == 0) {
            entity.lastWorldTime = world.getTime();
            return;
        }

        long now = world.getTime();
        long delta = now - entity.lastWorldTime;
        if (delta <= 0) {
            entity.lastWorldTime = now;
            return;
        }
        if (delta > MAX_DELTA) {
            delta = MAX_DELTA;
        }

        // 增加进度并更新时间基准
        entity.progress += delta;
        entity.lastWorldTime = now;

        // 处理进度增加后的所有副作用（周期完成、状态更新、物品喷出）
        entity.processProgress();
    }

    /**
     * 注入进度（仅服务端有效）
     *
     * @param amount 增加的进度（tick 数），必须 ≥ 0
     */
    public void addProgress(long amount) {
        if (world == null || world.isClient) return;
        if (amount <= 0) return;

        progress += amount;
        processProgress();
    }

    // ======================== 声音和粒子 ========================

    private static void playSound(World world, MimeticLeyLineBlockEntity entity, BlockPos pos) {
        if (world.isClient()) return;

        LootPoolConfig.PoolEntry entry = entity.currentEntry;
        if (entry == null) return;

        long cost = entry.cost();
        long progress = entity.progress;
        State state = entity.getState();

        // 萌发阶段
        if (state == State.BUDDING) {
            long buddingStart = entity.getBuddingPosition();  // 萌发阶段起始进度
            long elapsedInBudding = progress - buddingStart;
            // 每 160 ticks 触发一次
            if (elapsedInBudding >= 0 && elapsedInBudding % 160 == 0) {
                world.playSound(null, pos, ModSoundEvent.MLL_BUDDING_SHOCK, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
        // 喷涌阶段
        else if (state == State.ERUPT) {
            if (progress == cost) {
                // 喷涌开始瞬间
                world.playSound(null, pos, ModSoundEvent.MLL_ERUPT_SMOKE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, ModSoundEvent.MLL_ERUPT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else if (progress == cost + 120) {
                // 喷涌一半
                world.playSound(null, pos, ModSoundEvent.MLL_ERUPT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    private static void randomParticle(World world, BlockPos pos, MimeticLeyLineBlockEntity entity) {
        Random random = world.random;

        switch (entity.getState()) {
            case BUDDING -> {
                if (entity.buddingParticleTime > 0) {
                    buddingParticle(random, world, pos, 0.90F);
                    entity.buddingParticleTime --;
                }
            }
            case ERUPT -> {
                buddingParticle(random, world, pos, 0.85F);
                if (random.nextFloat() > 0.8) {
                    world.addParticle(ParticleTypes.LAVA,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            world.random.nextDouble(), 1.0, world.random.nextDouble());
                }
            }
        }
    }

    protected static void buddingParticle(Random random, World world, BlockPos pos, float Density) {
        if (random.nextFloat() > Density) {
            world.addParticle(ModParticleTypes.MIMETIC_LEY_LINE_DUST,
                    pos.getX() + createRandomPos(random), pos.getY() + 0.3, pos.getZ() + createRandomPos(random),
                    0.0, -0.2, 0.0);
        }
    }

    protected static float createRandomPos(Random random) {
        return 0.45F + (random.nextFloat() / 5);
    }

    // ======================== 动画处理 ========================

    protected void processAnimation() {
        State currentState = getState();

        switch (currentState) {
            case AMASS -> {
                amassAnimationState.startIfNotRunning(0);
                buddingAnimationState.reset();
                eruptAnimationState.reset();
            }
            case BUDDING -> {
                buddingAnimationState.startIfNotRunning(getBuddingPosition());
                amassAnimationState.reset();
                eruptAnimationState.reset();
            }
            case ERUPT -> {
                eruptAnimationState.startIfNotRunning(currentEntry.cost());
                // 倒放逻辑：喷涌过半后反转动画
                if (getEruptTicks() > ERUPT_DURATION / 2 && !eruptAnimationState.reversed) {
                    eruptAnimationState.reversed = true;
                }
                amassAnimationState.reset();
                buddingAnimationState.reset();
            }
        }
    }

    // ======================== 内部进度处理逻辑 ========================

    /**
     * 处理当前进度可能引起的周期完成、状态转换和物品喷出。
     * <p>
     * 该方法不依赖世界时间，仅基于当前 progress、currentEntry 等字段进行状态机更新。
     */
    private void processProgress() {
        // 处理可能跨越的多个完整周期
        while (currentEntry != null && progress >= currentEntry.cost() + ERUPT_DURATION) {
            completeCurrentCycle();
            startNewCycle();
        }

        // 如果没有激活的周期，开始新周期
        if (currentEntry == null) {
            startNewCycle();
        }

        // 更新运行状态
        updateState();

        // 喷涌阶段内，检查是否需要喷出物品
        if (state == State.ERUPT && !hasErupted && currentEntry != null) {
            long eruptProgress = progress - currentEntry.cost();
            if (eruptProgress >= ERUPT_SPAWN_OFFSET) {
                spawnItem();
                hasErupted = true;
            }
        }

        markDirty();
    }

    // ======================== 周期管理 ========================

    /**
     * 开始一个新的运行周期：抽选一个抽奖项，重置喷出标志，保留当前进度。
     */
    private void startNewCycle() {
        if (currentEntry != null) return;
        currentEntry = selectEntry();
        hasErupted = false;
        updateState();
    }

    /**
     * 完成当前周期：强制喷出未喷出的物品，扣除周期总进度，清空当前抽奖项。
     */
    private void completeCurrentCycle() {
        if (currentEntry == null) return;

        // 如果物品还没喷出（例如 delta 过大直接跳过了喷出点），强制喷出
        if (!hasErupted) {
            spawnItem();
            hasErupted = true;
        }

        long totalCost = currentEntry.cost() + ERUPT_DURATION;
        progress -= totalCost;

        // 清空当前周期，下一次 processProgress 会开始新周期
        currentEntry = null;
        hasErupted = false;
    }

    // ======================== 抽奖机制 ========================

    /**
     * 从配置的抽奖池中按照概率抽取一个项，带保底机制。
     *
     * @return 选中的抽奖项
     */
    private LootPoolConfig.PoolEntry selectEntry() {
        LootPoolConfig config = ConfigManager.get(ModConfigs.LOOT_POOL_CONFIG);

        List<LootPoolConfig.PoolEntry> entries = (config != null) ? config.entries() : null;
        if (entries == null || entries.isEmpty()) {
            OreDredging.LOGGER.warn("MimeticLeyLine at {}: No valid loot pool, using fallback", pos);
            return new LootPoolConfig.PoolEntry(new ItemStack(Items.STONE), 1.0f, 200);
        }

        for (int attempt = 0; attempt < MAX_SELECT_ATTEMPTS; attempt++) {
            LootPoolConfig.PoolEntry candidate = entries.get(RandomUtil.nextInt(entries.size()));
            if (RandomUtil.randomBoolean(candidate.probability())) {
                return candidate;
            }
        }

        OreDredging.LOGGER.warn("MimeticLeyLine at {}: Failed to select entry after {} attempts, using first entry",
                pos, MAX_SELECT_ATTEMPTS);
        return entries.get(0);
    }

    // ======================== 物品生成 ========================

    /**
     * 将当前抽奖项中的物品从方块中心向上抛出，附带随机水平速度。
     */
    private void spawnItem() {
        if (world == null || world.isClient) return;
        if (currentEntry == null) return;

        ItemStack stack = currentEntry.item().copy();
        Vec3d center = Vec3d.ofCenter(pos);

        Random random = world.random;
        double vx = (random.nextDouble() - 0.5) * 0.2;
        double vz = (random.nextDouble() - 0.5) * 0.2;
        double vy = 0.3;

        ItemEntity itemEntity = new ItemEntity(world, center.x, center.y, center.z, stack, vx, vy, vz);
        world.spawnEntity(itemEntity);
    }

    // ======================== 状态管理 ========================

    /**
     * 根据当前进度和抽奖项的 cost 更新运行状态。
     */
    private void updateState() {
        if (currentEntry == null) {
            state = State.AMASS;
            return;
        }
        long cost = currentEntry.cost();
        if (progress < cost * 2 / 3) {
            state = State.AMASS;
        } else if (progress < cost) {
            state = State.BUDDING;
        } else {
            state = State.ERUPT;
        }

        if (world != null) {
            world.setBlockState(pos, getCachedState().with(MimeticLeyLineBlock.STATE, getState()));
        }
    }

    // ======================== NBT 持久化 ========================

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("Progress", progress);
        nbt.putLong("LastWorldTime", lastWorldTime);
        nbt.putBoolean("HasErupted", hasErupted);
        if (currentEntry != null) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.put("Item", currentEntry.item().writeNbt(new NbtCompound()));
            entryNbt.putFloat("Probability", currentEntry.probability());
            entryNbt.putInt("Cost", currentEntry.cost());
            nbt.put("CurrentEntry", entryNbt);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        progress = nbt.getLong("Progress");
        lastWorldTime = nbt.getLong("LastWorldTime");
        hasErupted = nbt.getBoolean("HasErupted");
        if (nbt.contains("CurrentEntry")) {
            NbtCompound entryNbt = nbt.getCompound("CurrentEntry");
            ItemStack item = ItemStack.fromNbt(entryNbt.getCompound("Item"));
            float prob = entryNbt.getFloat("Probability");
            int cost = entryNbt.getInt("Cost");
            currentEntry = new LootPoolConfig.PoolEntry(item, prob, cost);
        } else {
            currentEntry = null;
        }
        updateState();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null && !this.world.isClient) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    // ======================== 运行状态 ========================

    /**
     * 获取当前运行状态。
     * @see State
     */
    public State getState() {
        return state;
    }

    public long getProgress() {
        return progress;
    }

    public void setBuddingParticleTime(int buddingParticleTime) {
        this.buddingParticleTime = buddingParticleTime;
    }

    /**
     * 获取当前周期的进度百分比（0.0 ~ 1.0），
     * @apiNote 仅用于 {@linkplain State#AMASS 积蓄} / {@linkplain State#BUDDING 萌发} 阶段。
     */
    public float getProgressPercent() {
        if (currentEntry == null) return 0;
        long cost = currentEntry.cost();
        if (progress < cost) return (float) progress / cost;
        else return 1.0f;
    }

    public int getBuddingPosition() {
        if (currentEntry == null) return 0;
        long cost = currentEntry.cost();

        return Math.toIntExact((cost * 2) / 3);
    }

    /**
     * 获取喷涌阶段已进行的 tick 数（0 ~ ERUPT_DURATION）。
     * @apiNote 仅当状态为 {@linkplain State#ERUPT 喷涌} 时有效。
     */
    public int getEruptTicks() {
        if (state != State.ERUPT || currentEntry == null) return 0;
        return (int) (progress - currentEntry.cost());
    }

    /**
     * 拟态地脉的运行阶段。
     * <ol>
     *     <li>{@linkplain #AMASS 积蓄}：缓慢旋转，前 2/3 进度</li>
     *     <li>{@linkplain #BUDDING 萌发}：不规则抽动，后 1/3 进度</li>
     *     <li>{@linkplain #ERUPT 喷涌}：打开并抛出物品，固定 12 秒</li>
     * </ol>
     */
    public enum State implements StringIdentifiable {
        AMASS("amass"),
        BUDDING("budding"),
        ERUPT("erupt");

        private final String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}