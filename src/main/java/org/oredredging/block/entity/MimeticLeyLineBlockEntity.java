package org.oredredging.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
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
import org.oredredging.entity.MimeticLeyLineFallingEntity;
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
    public static final int ERUPT_DURATION = 240;
    public static final int ERUPT_SPAWN_OFFSET = 100;
    public static final long MAX_DELTA = 24000L * 30;
    public static final int MAX_SELECT_ATTEMPTS = 100;
    public static final int MAX_MULTI_SELECT_ATTEMPTS = 50;

    // 客户端视觉高度相关
    public static final float MAX_FLOAT_HEIGHT = 0.35F;
    public static final float HEIGHT_CHANGE_SPEED = 0.02F;
    // 坠落实体速度
    public static final float FALL_SPEED = 0.06F;

    // 磁石检测
    public static final int MAGNET_CHECK_RADIUS = 4;
    public static final int MAGNET_CHECK_INTERVAL = 5;

    private long progress;
    private long lastWorldTime;
    private LootPoolConfig.PoolEntry primaryEntry;
    private final List<LootPoolConfig.PoolEntry> additionalEntries = new ArrayList<>();
    private boolean hasErupted;
    protected int buddingParticleTime;
    private State state = State.DORMANT;

    // 客户端高度
    private float currentHeight = 0.0F;

    // 动画状态
    public final EnhancedAnimationState amassAnimationState = new EnhancedAnimationState();
    public final EnhancedAnimationState buddingAnimationState = new EnhancedAnimationState();
    public final EnhancedAnimationState eruptAnimationState = new EnhancedAnimationState();

    public MimeticLeyLineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIMETIC_LEY_LINE, pos, state);
    }

    // ================== 核心 tick ==================
    public static void tick(World world, BlockPos pos, BlockState state, MimeticLeyLineBlockEntity entity) {
        randomParticle(world, pos, entity);
        playSound(world, entity, pos);

        // 服务端磁石检测
        if (!world.isClient && world.getTime() % MAGNET_CHECK_INTERVAL == 0) {
            entity.checkMagnetAndHandleState(world);
        }

        if (world.isClient()) {
            entity.updateClientHeight();
            entity.processAnimation();
            randomParticle(world, pos, entity);
            return;
        }

        if (entity.state == State.DORMANT) {
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

        playSound(world, entity, pos);
        entity.processProgress();
    }

    public void addProgress(long amount) {
        if (world == null || world.isClient || state == State.DORMANT) return;
        if (amount <= 0) return;
        progress += amount;
        processProgress();
    }

    // ================== 磁石检测与休眠 ==================
    private void checkMagnetAndHandleState(World world) {
        boolean hasMagnet = false;
        for (int i = 1; i <= MAGNET_CHECK_RADIUS; i++) {
            if (world.getBlockState(pos.down(i)).isOf(Blocks.LODESTONE)) {
                hasMagnet = true;
                break;
            }
        }

        if (hasMagnet) {
            if (state == State.DORMANT) {
                exitDormant();
            }
        } else {
            if (state != State.DORMANT) {
                enterDormant(world);
            }
        }
    }

    private void enterDormant(World world) {
        progress = 0;
        lastWorldTime = world.getTime();
        primaryEntry = null;
        additionalEntries.clear();
        hasErupted = false;
        state = State.DORMANT;

        amassAnimationState.reset();
        buddingAnimationState.reset();
        eruptAnimationState.reset();

        syncState(world);

        world.scheduleBlockTick(pos, getCachedState().getBlock(), 5);
    }

    private void exitDormant() {
        state = State.AMASS;
        startNewCycle();
        syncState(world);
    }

    private void syncState(World world) {
        if (world != null && !world.isClient) {
            BlockState newState = getCachedState().with(MimeticLeyLineBlock.STATE, state)
                    .with(MimeticLeyLineBlock.LIGHT, 0);
            world.setBlockState(pos, newState, Block.NOTIFY_ALL);
            markDirty();
        }
    }

    // ================== 坠落实体转换 ==================
    public void tryStartFalling(World world) {
        if (world.isClient) return;
        if (state != State.DORMANT) return;
        if (!FallingBlock.canFallThrough(world.getBlockState(pos.down()))) return;

        NbtCompound entityNbt = createNbt();
        world.removeBlock(pos, false);

        MimeticLeyLineFallingEntity fallingEntity = new MimeticLeyLineFallingEntity(
                world,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                getCachedState(),
                entityNbt
        );
        world.spawnEntity(fallingEntity);
    }

    public static void placeFromFalling(World world, BlockPos pos, BlockState blockState, NbtCompound entityNbt) {
        world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MimeticLeyLineBlockEntity mbe) {
            mbe.readNbt(entityNbt);
            mbe.currentHeight = MAX_FLOAT_HEIGHT;
            if (!world.isClient) {
                boolean hasMagnet = false;
                for (int i = 1; i <= MAGNET_CHECK_RADIUS; i++) {
                    if (world.getBlockState(pos.down(i)).isOf(Blocks.LODESTONE)) {
                        hasMagnet = true;
                        break;
                    }
                }
                if (hasMagnet) {
                    mbe.exitDormant();
                } else {
                    mbe.state = State.DORMANT;
                    mbe.progress = 0;
                    mbe.primaryEntry = null;
                    mbe.additionalEntries.clear();
                    mbe.hasErupted = false;
                    mbe.syncState(world);
                }
            }
        }
    }

    // ================== 客户端高度 ==================
    private void updateClientHeight() {
        if (state == State.DORMANT) {
            if (currentHeight > 0) {
                currentHeight = Math.max(0, currentHeight - HEIGHT_CHANGE_SPEED);
            }
        } else {
            if (currentHeight < MAX_FLOAT_HEIGHT) {
                currentHeight = Math.min(MAX_FLOAT_HEIGHT, currentHeight + HEIGHT_CHANGE_SPEED);
            }
        }
    }

    public float getRenderHeightOffset() {
        return currentHeight;
    }

    public void setBuddingParticleTime(int time) {
        this.buddingParticleTime = time;
    }

    // ================== 声音与粒子 ==================
    private static void playSound(World world, MimeticLeyLineBlockEntity entity, BlockPos pos) {
        if (world.isClient()) return;
        LootPoolConfig.PoolEntry entry = entity.primaryEntry;
        if (entry == null) return;

        long cost = entry.cost();
        long progress = entity.progress;
        State state = entity.getState();

        if (state == State.BUDDING) {
            long buddingStart = entity.getBuddingPosition();
            long elapsed = progress - buddingStart;
            if (elapsed >= 0 && elapsed % 160 == 0) {
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

    protected static void buddingParticle(Random random, World world, BlockPos pos, float density) {
        if (random.nextFloat() > density) {
            world.addParticle(ModParticleTypes.MIMETIC_LEY_LINE_DUST,
                    pos.getX() + createRandomPos(random), pos.getY() + 0.3, pos.getZ() + createRandomPos(random),
                    0.0, -0.2, 0.0);
        }
    }

    protected static float createRandomPos(Random random) {
        return 0.45F + (random.nextFloat() / 5);
    }

    // ================== 动画处理 ==================
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
            case DORMANT -> {
                amassAnimationState.reset();
                buddingAnimationState.reset();
                eruptAnimationState.reset();
            }
        }
    }

    // ================== 进度处理 ==================
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

    private void startNewCycle() {
        if (primaryEntry != null) return;

        List<LootPoolConfig.PoolEntry> selected = selectEntries(3);
        if (selected.isEmpty()) {
            OreDredging.LOGGER.warn("MimeticLeyLine at {}: Fallback entry", pos);
            primaryEntry = new LootPoolConfig.PoolEntry(new ItemStack(Items.STONE), 1.0f, 200);
            additionalEntries.clear();
        } else {
            primaryEntry = selected.stream()
                    .max((a, b) -> Integer.compare(a.cost(), b.cost()))
                    .orElse(selected.get(0));
            additionalEntries.clear();
            additionalEntries.addAll(selected);
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

    // ================== 抽奖逻辑 ==================
    private List<LootPoolConfig.PoolEntry> selectEntries(int count) {
        LootPoolConfig config = ConfigManager.get(ModConfigs.LOOT_POOL_CONFIG);
        List<LootPoolConfig.PoolEntry> allEntries = (config != null) ? config.entries() : null;
        if (allEntries == null || allEntries.isEmpty()) {
            OreDredging.LOGGER.warn("MimeticLeyLine at {}: No loot pool", pos);
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
                if (entry == null) { success = false; break; }
                result.add(entry);
                selectedSet.add(entry);
            }
            if (success && result.size() == count) return result;
        }

        // 降级
        OreDredging.LOGGER.warn("MimeticLeyLine at {}: Falling back to duplicate entries", pos);
        result.clear();
        while (result.size() < count) {
            LootPoolConfig.PoolEntry entry = selectSingleEntry(allEntries);
            if (entry != null) result.add(entry);
            else break;
        }
        return result;
    }

    private LootPoolConfig.PoolEntry selectSingleEntryExcluding(List<LootPoolConfig.PoolEntry> entries, Set<LootPoolConfig.PoolEntry> excluded) {
        if (entries.isEmpty()) return null;
        for (int i = 0; i < MAX_SELECT_ATTEMPTS; i++) {
            LootPoolConfig.PoolEntry candidate = entries.get(RandomUtil.nextInt(entries.size()));
            if (!excluded.contains(candidate) && RandomUtil.randomBoolean(candidate.probability())) {
                return candidate;
            }
        }
        for (LootPoolConfig.PoolEntry entry : entries) {
            if (!excluded.contains(entry)) return entry;
        }
        return null;
    }

    private LootPoolConfig.PoolEntry selectSingleEntry(List<LootPoolConfig.PoolEntry> entries) {
        if (entries.isEmpty()) return null;
        for (int i = 0; i < MAX_SELECT_ATTEMPTS; i++) {
            LootPoolConfig.PoolEntry candidate = entries.get(RandomUtil.nextInt(entries.size()));
            if (RandomUtil.randomBoolean(candidate.probability())) return candidate;
        }
        return entries.get(0);
    }

    // ================== 物品生成 ==================
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
        world.spawnEntity(new ItemEntity(world, center.x, center.y, center.z, stack, vx, vy, vz));
    }

    // ================== 状态更新 ==================
    private void updateState() {
        if (primaryEntry == null) {
            state = State.AMASS;
        } else {
            long cost = primaryEntry.cost();
            if (progress < cost * 2 / 3) state = State.AMASS;
            else if (progress < cost) state = State.BUDDING;
            else state = State.ERUPT;
        }

        if (world != null) {
            BlockState newState = getCachedState().with(MimeticLeyLineBlock.STATE, state);
            if (state == State.ERUPT) {
                newState = newState.with(MimeticLeyLineBlock.LIGHT, computeEruptBrightness(getEruptTicks()));
            } else {
                newState = newState.with(MimeticLeyLineBlock.LIGHT, 0);
            }
            world.setBlockState(pos, newState);
        }
    }

    private int computeEruptBrightness(int eruptTicks) {
        if (eruptTicks < 0 || eruptTicks >= ERUPT_DURATION) return 0;
        float brightness;
        if (eruptTicks <= 100) brightness = 4.0f * eruptTicks / 100.0f;
        else if (eruptTicks <= 140) brightness = 4.0f;
        else brightness = 4.0f * (ERUPT_DURATION - eruptTicks) / 100.0f;
        return Math.round(brightness);
    }

    // ================== NBT ==================
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("Progress", progress);
        nbt.putLong("LastWorldTime", lastWorldTime);
        nbt.putBoolean("HasErupted", hasErupted);
        nbt.putString("State", state.asString());

        if (primaryEntry != null) nbt.put("PrimaryEntry", entryToNbt(primaryEntry));
        NbtList additionalList = new NbtList();
        for (LootPoolConfig.PoolEntry entry : additionalEntries) {
            additionalList.add(entryToNbt(entry));
        }
        nbt.put("AdditionalEntries", additionalList);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        progress = nbt.getLong("Progress");
        lastWorldTime = nbt.getLong("LastWorldTime");
        hasErupted = nbt.getBoolean("HasErupted");
        state = nbt.contains("State") ? State.fromString(nbt.getString("State")) : State.AMASS;

        if (nbt.contains("PrimaryEntry")) primaryEntry = entryFromNbt(nbt.getCompound("PrimaryEntry"));
        else primaryEntry = null;

        additionalEntries.clear();
        NbtList additionalList = nbt.getList("AdditionalEntries", 10);
        for (int i = 0; i < additionalList.size(); i++) {
            additionalEntries.add(entryFromNbt(additionalList.getCompound(i)));
        }
    }

    private NbtCompound entryToNbt(LootPoolConfig.PoolEntry entry) {
        NbtCompound nbt = new NbtCompound();
        nbt.put("Item", entry.item().writeNbt(new NbtCompound()));
        nbt.putFloat("Probability", entry.probability());
        nbt.putInt("Cost", entry.cost());
        return nbt;
    }

    private LootPoolConfig.PoolEntry entryFromNbt(NbtCompound nbt) {
        return new LootPoolConfig.PoolEntry(
                ItemStack.fromNbt(nbt.getCompound("Item")),
                nbt.getFloat("Probability"),
                nbt.getInt("Cost")
        );
    }

    // ================== 网络同步 ==================
    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    // ================== 公开访问器 ==================
    public State getState() { return state; }
    public long getProgress() { return progress; }

    public float getProgressPercent() {
        if (primaryEntry == null) return 0;
        long cost = primaryEntry.cost();
        return progress < cost ? (float) progress / cost : 1.0f;
    }

    public int getBuddingPosition() {
        if (primaryEntry == null) return 0;
        return Math.toIntExact((primaryEntry.cost() * 2L) / 3);
    }

    public int getEruptTicks() {
        if (state != State.ERUPT || primaryEntry == null) return 0;
        return (int) (progress - primaryEntry.cost());
    }

    public int getOffEruptCenter() {
        if (state != State.ERUPT || primaryEntry == null) return 0;
        int center = ERUPT_DURATION / 2;
        int ticks = getEruptTicks();
        return Math.abs(ticks - center);
    }

    // ================== 状态枚举 ==================
    public enum State implements StringIdentifiable {
        AMASS("amass"),
        BUDDING("budding"),
        ERUPT("erupt"),
        DORMANT("dormant");

        private final String name;
        State(String name) { this.name = name; }

        @Override
        public String asString() { return name; }

        public static State fromString(String name) {
            for (State s : values()) {
                if (s.asString().equals(name)) return s;
            }
            return AMASS;
        }
    }
}