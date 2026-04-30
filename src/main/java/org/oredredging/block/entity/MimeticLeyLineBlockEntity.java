package org.oredredging.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MimeticLeyLineBlockEntity extends BlockEntity {
    /** 喷涌阶段固定时长（ticks），12秒 = 240 ticks */
    private static final int ERUPT_DURATION = 240;
    /** 喷涌开始后多久喷出物品（ticks），5秒 = 100 ticks */
    private static final int ERUPT_SPAWN_OFFSET = 100;
    /** 最大单次进度补偿（ticks），防止单次 delta 过大导致性能问题或溢出 */
    private static final long MAX_DELTA = 24000L * 30; // 30天
    /** 抽奖最大尝试次数，防止极端概率下无限循环 */
    private static final int MAX_SELECT_ATTEMPTS = 100;
    /** 抽取多个条目时的额外尝试次数 */
    private static final int MAX_MULTI_SELECT_ATTEMPTS = 50;

    /** 当前周期已累计的进度（ticks） */
    private long progress;
    /** 上次 tick 时的世界总时间（用于计算 delta） */
    private long lastWorldTime;
    /** 当前周期的主抽奖项（cost 最大） */
    private LootPoolConfig.PoolEntry primaryEntry;
    /** 当前周期的附加抽奖项（其余两个） */
    private List<LootPoolConfig.PoolEntry> additionalEntries = new ArrayList<>();
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

    public static void tick(World world, BlockPos pos, BlockState state, MimeticLeyLineBlockEntity entity) {
        randomParticle(world, pos, entity);
        playSound(world, entity, pos);

        if (world.isClient()) {
            entity.processAnimation();
            return;
        }

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

        entity.progress += delta;
        entity.lastWorldTime = now;
        entity.processProgress();
    }

    public void addProgress(long amount) {
        if (world == null || world.isClient) return;
        if (amount <= 0) return;
        progress += amount;
        processProgress();
    }

    // ======================== 声音和粒子 ========================

    private static void playSound(World world, MimeticLeyLineBlockEntity entity, BlockPos pos) {
        if (world.isClient()) return;

        LootPoolConfig.PoolEntry entry = entity.primaryEntry;
        if (entry == null) return;

        long cost = entry.cost();
        long progress = entity.progress;
        State state = entity.getState();

        if (state == State.BUDDING) {
            long buddingStart = entity.getBuddingPosition();
            long elapsedInBudding = progress - buddingStart;
            if (elapsedInBudding >= 0 && elapsedInBudding % 160 == 0) {
                world.playSound(null, pos, ModSoundEvent.MLL_BUDDING_SHOCK, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
        } else if (state == State.ERUPT) {
            if (progress == cost) {
                world.playSound(null, pos, ModSoundEvent.MLL_ERUPT_SMOKE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.playSound(null, pos, ModSoundEvent.MLL_ERUPT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else if (progress == cost + 120) {
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
                    entity.buddingParticleTime--;
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
                eruptAnimationState.startIfNotRunning(primaryEntry.cost());
                if (getEruptTicks() > ERUPT_DURATION / 2 && !eruptAnimationState.reversed) {
                    eruptAnimationState.reversed = true;
                }
                amassAnimationState.reset();
                buddingAnimationState.reset();
            }
        }
    }

    // ======================== 内部进度处理逻辑 ========================

    private void processProgress() {
        while (primaryEntry != null && progress >= primaryEntry.cost() + ERUPT_DURATION) {
            completeCurrentCycle();
            startNewCycle();
        }

        if (primaryEntry == null) {
            startNewCycle();
        }

        updateState();

        if (state == State.ERUPT && !hasErupted && primaryEntry != null) {
            long eruptProgress = progress - primaryEntry.cost();
            if (eruptProgress >= ERUPT_SPAWN_OFFSET) {
                spawnItems();
                hasErupted = true;
            }
        }

        markDirty();
    }

    // ======================== 周期管理 ========================

    private void startNewCycle() {
        if (primaryEntry != null) return;

        List<LootPoolConfig.PoolEntry> selected = selectEntries(3);
        if (selected.isEmpty()) {
            OreDredging.LOGGER.warn("MimeticLeyLine at {}: Failed to select any entry, using fallback", pos);
            primaryEntry = new LootPoolConfig.PoolEntry(new ItemStack(Items.STONE), 1.0f, 200);
            additionalEntries.clear();
        } else {
            // 找出 cost 最大的作为主条目
            primaryEntry = selected.stream().max((a, b) -> Integer.compare(a.cost(), b.cost())).orElse(selected.get(0));
            additionalEntries = new ArrayList<>(selected);
            additionalEntries.remove(primaryEntry);
        }

        hasErupted = false;
        updateState();
    }

    private void completeCurrentCycle() {
        if (primaryEntry == null) return;

        if (!hasErupted) {
            spawnItems();
            hasErupted = true;
        }

        long totalCost = primaryEntry.cost() + ERUPT_DURATION;
        progress -= totalCost;

        primaryEntry = null;
        additionalEntries.clear();
        hasErupted = false;
    }

    // ======================== 抽奖机制（多条目，不重复） ========================

    /**
     * 从配置的抽奖池中抽取指定数量的不重复条目，每个条目独立概率判定。
     * 返回的条目数量可能少于请求数量（如果池子不足或尝试超限时降级）。
     *
     * @param count 期望抽取的条目数量
     * @return 抽取到的条目列表（可能为空或不足数量）
     */
    private List<LootPoolConfig.PoolEntry> selectEntries(int count) {
        LootPoolConfig config = ConfigManager.get(ModConfigs.LOOT_POOL_CONFIG);
        List<LootPoolConfig.PoolEntry> allEntries = (config != null) ? config.entries() : null;
        if (allEntries == null || allEntries.isEmpty()) {
            OreDredging.LOGGER.warn("MimeticLeyLine at {}: No valid loot pool", pos);
            return List.of();
        }

        List<LootPoolConfig.PoolEntry> result = new ArrayList<>();
        Set<LootPoolConfig.PoolEntry> selectedSet = new HashSet<>();

        for (int attempt = 0; attempt < MAX_MULTI_SELECT_ATTEMPTS && result.size() < count; attempt++) {
            result.clear();
            selectedSet.clear();
            boolean success = true;

            for (int i = 0; i < count; i++) {
                LootPoolConfig.PoolEntry entry = selectSingleEntryExcluding(allEntries, selectedSet);
                if (entry == null) {
                    success = false;
                    break;
                }
                result.add(entry);
                selectedSet.add(entry);
            }

            if (success && result.size() == count) {
                return result;
            }
        }

        // 降级：允许重复，用普通抽奖补全不足的数量
        OreDredging.LOGGER.warn("MimeticLeyLine at {}: Could not select {} distinct entries, falling back to allow duplicates", pos, count);
        while (result.size() < count) {
            LootPoolConfig.PoolEntry entry = selectSingleEntry(allEntries);
            if (entry != null) {
                result.add(entry);
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * 从条目列表中独立抽取一个条目（概率判定），排除指定的已选条目。
     * 带保底机制：尝试 MAX_SELECT_ATTEMPTS 次后返回第一个未被排除的条目。
     *
     * @param entries  候选条目池
     * @param excluded 已选中的条目集合（不能返回这些条目）
     * @return 选中的条目，无可用时返回 null
     */
    private LootPoolConfig.PoolEntry selectSingleEntryExcluding(List<LootPoolConfig.PoolEntry> entries, Set<LootPoolConfig.PoolEntry> excluded) {
        if (entries.isEmpty()) return null;

        for (int attempt = 0; attempt < MAX_SELECT_ATTEMPTS; attempt++) {
            LootPoolConfig.PoolEntry candidate = entries.get(RandomUtil.nextInt(entries.size()));
            if (!excluded.contains(candidate) && RandomUtil.randomBoolean(candidate.probability())) {
                return candidate;
            }
        }

        // 保底：返回第一个未被排除的条目
        for (LootPoolConfig.PoolEntry entry : entries) {
            if (!excluded.contains(entry)) {
                return entry;
            }
        }
        return null; // 所有条目都被排除（理论上不会发生）
    }

    /**
     * 原始的单次抽奖（允许重复，不考虑排除），用于降级处理。
     */
    private LootPoolConfig.PoolEntry selectSingleEntry(List<LootPoolConfig.PoolEntry> entries) {
        if (entries.isEmpty()) return null;

        for (int attempt = 0; attempt < MAX_SELECT_ATTEMPTS; attempt++) {
            LootPoolConfig.PoolEntry candidate = entries.get(RandomUtil.nextInt(entries.size()));
            if (RandomUtil.randomBoolean(candidate.probability())) {
                return candidate;
            }
        }
        return entries.get(0);
    }

    // ======================== 物品生成 ========================

    private void spawnItems() {
        if (world == null || world.isClient) return;

        spawnItem(primaryEntry);
        for (LootPoolConfig.PoolEntry entry : additionalEntries) {
            spawnItem(entry);
        }
    }

    private void spawnItem(LootPoolConfig.PoolEntry entry) {
        if (entry == null) return;
        ItemStack stack = entry.item().copy();
        Vec3d center = Vec3d.ofCenter(pos);

        Random random = world.random;
        double vx = (random.nextDouble() - 0.5) * 0.2;
        double vz = (random.nextDouble() - 0.5) * 0.2;
        double vy = 0.3;

        ItemEntity itemEntity = new ItemEntity(world, center.x, center.y, center.z, stack, vx, vy, vz);
        world.spawnEntity(itemEntity);
    }

    // ======================== 状态管理 ========================

    private void updateState() {
        if (primaryEntry == null) {
            state = State.AMASS;
        } else {
            long cost = primaryEntry.cost();
            if (progress < cost * 2 / 3) {
                state = State.AMASS;
            } else if (progress < cost) {
                state = State.BUDDING;
            } else {
                state = State.ERUPT;
            }
        }

        if (world != null) {
            BlockState newState = getCachedState().with(MimeticLeyLineBlock.STATE, state);
            if (state == State.ERUPT) {
                int light = computeEruptBrightness(getEruptTicks());
                newState = newState.with(MimeticLeyLineBlock.LIGHT, light);
            } else {
                newState = newState.with(MimeticLeyLineBlock.LIGHT, 0);
            }
            world.setBlockState(pos, newState);
        }
    }

    private int computeEruptBrightness(int eruptTicks) {
        if (eruptTicks < 0 || eruptTicks >= ERUPT_DURATION) {
            return 0;
        }
        float brightness;
        if (eruptTicks <= 100) {               // 0～5秒：线性增加
            brightness = 4.0f * eruptTicks / 100.0f;
        } else if (eruptTicks <= 140) {        // 5～7秒：完全打开
            brightness = 4.0f;
        } else {                               // 7～12秒：线性减少
            brightness = 4.0f * (ERUPT_DURATION - eruptTicks) / 100.0f;
        }
        return Math.round(brightness);
    }

    // ======================== NBT 持久化 ========================

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("Progress", progress);
        nbt.putLong("LastWorldTime", lastWorldTime);
        nbt.putBoolean("HasErupted", hasErupted);

        if (primaryEntry != null) {
            nbt.put("PrimaryEntry", entryToNbt(primaryEntry));
        }
        NbtList additionalList = new NbtList();
        for (LootPoolConfig.PoolEntry entry : additionalEntries) {
            additionalList.add(entryToNbt(entry));
        }
        nbt.put("AdditionalEntries", additionalList);
    }

    private NbtCompound entryToNbt(LootPoolConfig.PoolEntry entry) {
        NbtCompound entryNbt = new NbtCompound();
        entryNbt.put("Item", entry.item().writeNbt(new NbtCompound()));
        entryNbt.putFloat("Probability", entry.probability());
        entryNbt.putInt("Cost", entry.cost());
        return entryNbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        progress = nbt.getLong("Progress");
        lastWorldTime = nbt.getLong("LastWorldTime");
        hasErupted = nbt.getBoolean("HasErupted");

        if (nbt.contains("PrimaryEntry")) {
            primaryEntry = entryFromNbt(nbt.getCompound("PrimaryEntry"));
        } else {
            primaryEntry = null;
        }

        additionalEntries.clear();
        NbtList additionalList = nbt.getList("AdditionalEntries", 10);
        for (int i = 0; i < additionalList.size(); i++) {
            NbtCompound entryNbt = additionalList.getCompound(i);
            additionalEntries.add(entryFromNbt(entryNbt));
        }

        updateState();
    }

    private LootPoolConfig.PoolEntry entryFromNbt(NbtCompound nbt) {
        ItemStack item = ItemStack.fromNbt(nbt.getCompound("Item"));
        float prob = nbt.getFloat("Probability");
        int cost = nbt.getInt("Cost");
        return new LootPoolConfig.PoolEntry(item, prob, cost);
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

    public State getState() {
        return state;
    }

    public long getProgress() {
        return progress;
    }

    public void setBuddingParticleTime(int buddingParticleTime) {
        this.buddingParticleTime = buddingParticleTime;
    }

    public float getProgressPercent() {
        if (primaryEntry == null) return 0;
        long cost = primaryEntry.cost();
        if (progress < cost) return (float) progress / cost;
        else return 1.0f;
    }

    public int getBuddingPosition() {
        if (primaryEntry == null) return 0;
        long cost = primaryEntry.cost();
        return Math.toIntExact((cost * 2) / 3);
    }

    public int getEruptTicks() {
        if (state != State.ERUPT || primaryEntry == null) return 0;
        return (int) (progress - primaryEntry.cost());
    }

    public int getOffEruptCenter() {
        if (state != State.ERUPT || primaryEntry == null) return 0;
        int center = ERUPT_DURATION / 2;
        int ticks = getEruptTicks();
        if (ticks > center) {
            return ticks - center;
        } else {
            return center - ticks;
        }
    }

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