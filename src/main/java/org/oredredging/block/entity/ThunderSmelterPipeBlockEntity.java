package org.oredredging.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
    private int craftingTicksRemaining = 0;  // 剩余熔炼 ticks，0 表示未在熔炼
    private final Random random = new Random();

    public ThunderSmelterPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THUNDER_SMELTER_PIPE, pos, state);
        this.listener = new Listener(new BlockPositionSource(pos));
    }

    // ========== Inventory 实现 ==========
    @Override
    public int size() {
        return 3;
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
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
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
            return new int[]{1, 2}; // 从下方可提取输出和额外输出槽
        } else {
            return new int[]{0};    // 从其他方向只能与输入槽交互
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        // 运行中禁止插入；仅输入槽且方向不是下方时允许
        return !isCrafting() && slot == 0 && dir != Direction.DOWN;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        // 运行中禁止提取；仅下方允许提取输出槽和额外输出槽
        return !isCrafting() && dir == Direction.DOWN && (slot == 1 || slot == 2);
    }

    // ========== 熔炼状态辅助 ==========
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

    // ========== 闪电监听与启动熔炼 ==========
    @Override
    public ThunderSmelterPipeBlockEntity.Listener getEventListener() {
        return listener;
    }

    public void onLightningStrike(ServerWorld world, Vec3d emitterPos) {
        if (isCrafting()) {
            return; // 已在熔炼，无视新雷击
        }
        startCraftingIfPossible(world);
    }

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

        // 模拟消耗1个输入物后的主产物
        ItemStack primaryOutput = recipe.craft(this, world.getRegistryManager());
        // 检查输出槽是否能容纳主产物
        ItemStack outputStack = inventory.get(1);
        if (!outputStack.isEmpty() && (!ItemStack.areItemsEqual(outputStack, primaryOutput) || outputStack.getCount() + primaryOutput.getCount() > outputStack.getMaxCount())) {
            return;
        }
        // 检查额外产物（按最大数量）
        ItemStack extraOutput = recipe.getExtraOutput(random);
        int extraCount = extraOutput.getCount();
        if (extraCount > 0) {
            ItemStack extraSlot = inventory.get(2);
            if (!extraSlot.isEmpty() && (!ItemStack.areItemsEqual(extraSlot, extraOutput) || extraSlot.getCount() + extraCount > extraSlot.getMaxCount())) {
                return;
            }
        }

        // 所有条件满足，开始熔炼
        craftingTicksRemaining = 100; // 5 秒
        setCrafting(true);
        markDirty();
    }

    // ========== 右键交互（来自方块类的委托） ==========
    public void interact(PlayerEntity player) {
        if (world == null || world.isClient) return;

        ItemStack held = player.getMainHandStack();
        // 优先尝试取出输入槽物品
        if (!inventory.get(0).isEmpty()) {
            // 给玩家
            ItemStack extracted = removeStack(0);
            if (!player.getInventory().insertStack(extracted)) {
                // 玩家背包已满，掉落
                player.dropItem(extracted, false);
            }
        } else if (!held.isEmpty()) {
            // 输入槽空，尝试放入玩家手中物品
            ItemStack toInsert = held.copy();
            int canInsert = Math.min(toInsert.getCount(), 21 - inventory.get(0).getCount()); // 输入槽最大堆叠21
            if (canInsert > 0) {
                player.getInventory().removeStack(player.getInventory().selectedSlot, canInsert);
                inventory.get(0).increment(canInsert);
            }
        }
        markDirty();
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
        // 产出主产物
        ItemStack primary = recipe.getOutput(world.getRegistryManager()).copy();
        if (inventory.get(1).isEmpty()) {
            inventory.set(1, primary);
        } else {
            inventory.get(1).increment(primary.getCount());
        }
        // 产出随机额外产物
        ItemStack extra = recipe.getExtraOutput(random);
        if (!extra.isEmpty()) {
            if (inventory.get(2).isEmpty()) {
                inventory.set(2, extra);
            } else {
                inventory.get(2).increment(extra.getCount());
            }
        }
        markDirty();
    }

    // ========== 序列化 ==========
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        craftingTicksRemaining = nbt.getInt("CraftingTicks");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("CraftingTicks", craftingTicksRemaining);
    }

    // ========== 内部监听器 ==========
    public static class Listener implements GameEventListener {
        private final PositionSource positionSource;

        public Listener(PositionSource positionSource) {
            this.positionSource = positionSource;
        }

        @Override
        public PositionSource getPositionSource() {
            return positionSource;
        }

        @Override
        public int getRange() {
            return 2;
        }

        @Override
        public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
            if (event == GameEvent.LIGHTNING_STRIKE) {
                // 获取方块实体并调用
                if (positionSource.getPos(world).isPresent()) {
                    Vec3d o = positionSource.getPos(world).get();
                    BlockPos pos = new BlockPos((int) o.getX(), (int) o.getY(), (int) o.getZ());
                    if (world.getBlockEntity(pos) instanceof ThunderSmelterPipeBlockEntity be) {
                        be.onLightningStrike(world, emitterPos);
                    }
                }
                return true;
            }
            return false;
        }
    }
}