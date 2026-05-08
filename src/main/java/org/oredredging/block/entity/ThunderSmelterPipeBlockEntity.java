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

public class ThunderSmelterPipeBlockEntity extends BlockEntity
        implements GameEventListener.Holder<ThunderSmelterPipeBlockEntity.Listener>, Inventory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private final Listener listener;
    private int craftingTicksRemaining = 0;
    private final Random random = new Random();

    private ItemStack pendingPrimaryOutput = ItemStack.EMPTY;
    private ItemStack pendingExtraOutput = ItemStack.EMPTY;

    public ThunderSmelterPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THUNDER_SMELTER_PIPE, pos, state);
        this.listener = new Listener(pos);
    }

    // ========== 槽位最大容量 ==========
    public int getSlotMaxCount(int slot) {
        return switch (slot) {
            case 0, 1 -> 3;   // 输入和主输出
            case 2 -> 10;     // 额外输出
            default -> 64;
        };
    }

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
        if (side == Direction.DOWN) {
            return new int[]{1, 2};
        } else {
            return new int[]{0};
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (isCrafting() || slot != 0 || dir == Direction.DOWN) {
            return false;
        }
        return getStack(1).isEmpty() && getStack(2).isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (isCrafting() || dir != Direction.DOWN || (slot != 1 && slot != 2)) {
            return false;
        }
        return getStack(0).isEmpty();
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
     * 熔炼完成：消耗输入槽全部物品，放入预先计算好的产物。
     */
    private void finishCrafting() {
        if (world == null || world.isClient) return;
        if (pendingPrimaryOutput.isEmpty() && pendingExtraOutput.isEmpty()) return;

        // 全部消耗输入槽物品
        inventory.get(0).setCount(0);

        // 放入主产物
        if (!pendingPrimaryOutput.isEmpty()) {
            ItemStack outputStack = inventory.get(1);
            int maxPrimary = getSlotMaxCount(1);
            if (outputStack.isEmpty()) {
                ItemStack toPut = pendingPrimaryOutput.copy();
                if (toPut.getCount() > maxPrimary) toPut.setCount(maxPrimary);
                inventory.set(1, toPut);
            } else if (ItemStack.areItemsEqual(outputStack, pendingPrimaryOutput)) {
                int newCount = outputStack.getCount() + pendingPrimaryOutput.getCount();
                if (newCount > maxPrimary) newCount = maxPrimary;
                outputStack.setCount(newCount);
            }
        }

        // 放入额外产物
        if (!pendingExtraOutput.isEmpty()) {
            ItemStack extraStack = inventory.get(2);
            int maxExtra = getSlotMaxCount(2);
            if (extraStack.isEmpty()) {
                ItemStack toPut = pendingExtraOutput.copy();
                if (toPut.getCount() > maxExtra) toPut.setCount(maxExtra);
                inventory.set(2, toPut);
            } else if (ItemStack.areItemsEqual(extraStack, pendingExtraOutput)) {
                int newCount = extraStack.getCount() + pendingExtraOutput.getCount();
                if (newCount > maxExtra) newCount = maxExtra;
                extraStack.setCount(newCount);
            }
        }

        // 清空暂存
        pendingPrimaryOutput = ItemStack.EMPTY;
        pendingExtraOutput = ItemStack.EMPTY;
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
     * 检查配方，根据输入槽总数量计算总产出，验证输出槽空间，都满足则暂存产物并开始熔炼。
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

        int inputCount = inputStack.getCount();
        ItemStack singlePrimary = recipe.getOutput(world.getRegistryManager()).copy();
        // 获取额外产物（只调用一次随机）
        ItemStack singleExtra = recipe.getExtraOutput(random).copy();

        // 计算总产物数量
        ItemStack totalPrimary = singlePrimary.copy();
        totalPrimary.setCount(singlePrimary.getCount() * inputCount);
        ItemStack totalExtra = singleExtra.copy();
        if (!singleExtra.isEmpty()) {
            totalExtra.setCount(singleExtra.getCount() * inputCount);
        }

        // 检查主输出槽空间
        ItemStack outputStack = inventory.get(1);
        int maxPrimary = getSlotMaxCount(1);
        if (!outputStack.isEmpty()) {
            if (!ItemStack.areItemsEqual(outputStack, totalPrimary)) {
                return;
            }
            if (outputStack.getCount() + totalPrimary.getCount() > maxPrimary) {
                return;
            }
        }

        // 检查额外输出槽空间
        if (!totalExtra.isEmpty()) {
            ItemStack extraSlot = inventory.get(2);
            int maxExtra = getSlotMaxCount(2);
            if (!extraSlot.isEmpty()) {
                if (!ItemStack.areItemsEqual(extraSlot, totalExtra)) {
                    return;
                }
                if (extraSlot.getCount() + totalExtra.getCount() > maxExtra) {
                    return;
                }
            }
        }

        // 暂存产物并启动熔炼
        pendingPrimaryOutput = totalPrimary;
        pendingExtraOutput = totalExtra;
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
        if (nbt.contains("PendingPrimary")) {
            pendingPrimaryOutput = ItemStack.fromNbt(nbt.getCompound("PendingPrimary"));
        } else {
            pendingPrimaryOutput = ItemStack.EMPTY;
        }
        if (nbt.contains("PendingExtra")) {
            pendingExtraOutput = ItemStack.fromNbt(nbt.getCompound("PendingExtra"));
        } else {
            pendingExtraOutput = ItemStack.EMPTY;
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("CraftingTicks", craftingTicksRemaining);
        if (!pendingPrimaryOutput.isEmpty()) {
            nbt.put("PendingPrimary", pendingPrimaryOutput.writeNbt(new NbtCompound()));
        }
        if (!pendingExtraOutput.isEmpty()) {
            nbt.put("PendingExtra", pendingExtraOutput.writeNbt(new NbtCompound()));
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