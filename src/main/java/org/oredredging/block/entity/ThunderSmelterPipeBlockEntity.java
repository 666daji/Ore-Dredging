package org.oredredging.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.oredredging.block.ThunderSmelterPipeBlock;
import org.oredredging.registry.ModBlockEntities;
import org.oredredging.registry.ModRecipes;
import org.oredredging.recipe.ThunderSmeltRecipe;

import java.util.Optional;
import java.util.Random;

/**
 * 雷霆熔炼管道的方块实体。
 * <p>
 * 拥有三个物品槽位：0 = 输入（最大堆叠3），1 = 主输出（最大堆叠3），2 = 额外输出（最大堆叠10）。
 * 接收到 {@link GameEvent#LIGHTNING_STRIKE} 事件时，若存在匹配的雷霆熔炼配方且输出槽有空间，
 * 则开始 5 秒（100 ticks）的熔炼过程。期间外部自动化（漏斗等）无法插入/提取。
 * 熔炼完成后消耗 1 个输入物品，生成主产物和随机额外产物。
 */
public class ThunderSmelterPipeBlockEntity extends BlockEntity
        implements GameEventListener.Holder<ThunderSmelterPipeBlockEntity.Listener>, Inventory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private final Listener listener;
    private int craftingTicksRemaining = 0;
    private final Random random = new Random();

    public ThunderSmelterPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THUNDER_SMELTER_PIPE, pos, state);
        this.listener = new Listener(pos);
    }

    // ========== 槽位最大容量 ==========

    /**
     * 获取指定槽位的最大堆叠数量。
     */
    public int getSlotMaxCount(int slot) {
        return switch (slot) {
            case 0, 1 -> 3;   // 输入和主输出
            case 2 -> 10;     // 额外输出
            default -> 64;
        };
    }

    /**
     * 告知外部自动化（如漏斗）此容器允许的最大堆叠数。
     * 这里选择返回 3，因为自动化只会向输入槽插入物品，输入槽上限为 3。
     * 对于只能内部产出的槽位 1 和 2，不受此限制影响。
     */
    @Override
    public int getMaxCountPerStack() {
        return 3;
    }

    // ========== Inventory 实现 ==========
    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        int max = getSlotMaxCount(slot);
        if (stack.getCount() > max) {
            stack.setCount(max);
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    // ========== SidedInventory 实现 ==========
    @Override
    public int[] getAvailableSlots(Direction side) {
        // 底面：允许访问输出槽；其他面：仅允许访问输入槽
        if (side == Direction.DOWN) {
            return new int[]{1, 2};
        } else {
            return new int[]{0};
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        // 仅在非熔炼状态、槽位为输入、且方向不是底面时允许插入
        return !isCrafting() && slot == 0 && dir != Direction.DOWN;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        // 仅在非熔炼状态、方向为底面、且槽位为输出槽时允许提取
        return !isCrafting() && dir == Direction.DOWN && (slot == 1 || slot == 2);
    }

    // ========== 熔炼状态与方块同步 ==========
    public boolean isCrafting() {
        return craftingTicksRemaining > 0;
    }

    public void setCrafting(boolean crafting) {
        if (world != null && !world.isClient) {
            BlockState state = getCachedState();
            if (state.contains(ThunderSmelterPipeBlock.CRAFTING)) {
                world.setBlockState(pos, state.with(ThunderSmelterPipeBlock.CRAFTING, crafting), 3);
            }
        }
    }

    // ========== Tick 逻辑 ==========
    public static void tick(World world, BlockPos pos, BlockState state, ThunderSmelterPipeBlockEntity be) {
        if (world.isClient) return;

        if (be.craftingTicksRemaining > 0) {
            be.craftingTicksRemaining--;
            if (be.craftingTicksRemaining == 0) {
                be.finishCrafting();
                be.setCrafting(false);
            }
            be.markDirty();
        }
    }

    /**
     * 熔炼完成：消耗1个输入物品，将主产物和额外产物放入对应槽位。
     */
    private void finishCrafting() {
        if (world == null || world.isClient) return;

        Optional<ThunderSmeltRecipe> match = world.getRecipeManager()
                .listAllOfType(ModRecipes.THUNDER_SMELT)
                .stream()
                .filter(recipe -> recipe.matches(this, world))
                .findFirst();

        if (match.isEmpty()) return;

        ThunderSmeltRecipe recipe = match.get();
        // 消耗输入
        inventory.get(0).decrement(1);

        // 产出主产物 (槽1，最大3)
        ItemStack primary = recipe.getOutput(world.getRegistryManager()).copy();
        ItemStack outputStack = inventory.get(1);
        int maxPrimary = getSlotMaxCount(1);
        if (outputStack.isEmpty()) {
            if (primary.getCount() > maxPrimary) primary.setCount(maxPrimary);
            inventory.set(1, primary);
        } else {
            int newCount = outputStack.getCount() + primary.getCount();
            if (newCount > maxPrimary) {
                outputStack.setCount(maxPrimary);
            } else {
                outputStack.increment(primary.getCount());
            }
        }

        // 产出随机额外产物 (槽2，最大10)
        ItemStack extra = recipe.getExtraOutput(random);
        if (!extra.isEmpty()) {
            ItemStack extraStack = inventory.get(2);
            int maxExtra = getSlotMaxCount(2);
            if (extraStack.isEmpty()) {
                if (extra.getCount() > maxExtra) extra.setCount(maxExtra);
                inventory.set(2, extra);
            } else {
                int newCount = extraStack.getCount() + extra.getCount();
                if (newCount > maxExtra) {
                    extraStack.setCount(maxExtra);
                } else {
                    extraStack.increment(extra.getCount());
                }
            }
        }
        markDirty();
    }

    // ========== 闪电监听与启动熔炼 ==========
    private void onLightningStrike(ServerWorld world, Vec3d emitterPos) {
        if (isCrafting()) {
            return;
        }
        startCraftingIfPossible(world);
    }

    /**
     * 检查配方并验证输出槽空间，满足条件则开始熔炼。
     */
    private void startCraftingIfPossible(ServerWorld world) {
        if (world == null) return;

        Optional<ThunderSmeltRecipe> match = world.getRecipeManager()
                .listAllOfType(ModRecipes.THUNDER_SMELT)
                .stream()
                .filter(recipe -> recipe.matches(this, world))
                .findFirst();

        if (match.isEmpty()) return;

        ThunderSmeltRecipe recipe = match.get();
        ItemStack inputStack = inventory.get(0);
        if (inputStack.isEmpty()) return;

        // 模拟主产物，检查主输出槽（1）空间
        ItemStack primaryOutput = recipe.craft(this, world.getRegistryManager());
        ItemStack outputStack = inventory.get(1);
        int maxPrimary = getSlotMaxCount(1);
        if (!outputStack.isEmpty() && (!ItemStack.areItemsEqual(outputStack, primaryOutput)
                || outputStack.getCount() + primaryOutput.getCount() > maxPrimary)) {
            return;
        }

        // 检查额外输出槽（2）空间
        ItemStack extraOutput = recipe.getExtraOutput(random);
        int extraCount = extraOutput.getCount();
        if (extraCount > 0) {
            ItemStack extraSlot = inventory.get(2);
            int maxExtra = getSlotMaxCount(2);
            if (!extraSlot.isEmpty() && (!ItemStack.areItemsEqual(extraSlot, extraOutput)
                    || extraSlot.getCount() + extraCount > maxExtra)) {
                return;
            }
        }

        // 条件满足，开始熔炼
        craftingTicksRemaining = 200;
        setCrafting(true);
        markDirty();
    }

    // ========== GameEventListener.Holder ==========
    @Override
    public ThunderSmelterPipeBlockEntity.Listener getEventListener() {
        return listener;
    }

    // ========== 序列化 ==========
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        inventory.clear();
        Inventories.readNbt(nbt, this.inventory);
        craftingTicksRemaining = nbt.getInt("CraftingTicks");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("CraftingTicks", craftingTicksRemaining);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null && !this.world.isClient) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    // ========== 内部监听器 ==========
    public static class Listener implements GameEventListener {
        private final BlockPos pos;

        public Listener(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public PositionSource getPositionSource() {
            return new BlockPositionSource(pos);
        }

        @Override
        public int getRange() {
            return 2;
        }

        @Override
        public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
            if (event == GameEvent.LIGHTNING_STRIKE) {
                if (world.getBlockEntity(pos) instanceof ThunderSmelterPipeBlockEntity be) {
                    be.onLightningStrike(world, emitterPos);
                    return true;
                }
            }
            return false;
        }
    }
}