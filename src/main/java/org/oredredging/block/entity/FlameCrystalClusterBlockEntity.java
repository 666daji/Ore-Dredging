package org.oredredging.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.oredredging.registry.ModBlockEntities;

public class FlameCrystalClusterBlockEntity extends BlockEntity {
    private int brushCount;
    protected boolean isNatural = true;
    private static final int REQUIRED_BRUSHES = 3;

    public FlameCrystalClusterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLAME_CRYSTAL_CLUSTER, pos, state);
    }

    public boolean brush(long time, PlayerEntity user, Direction side) {
        if (world == null || world.isClient) {
            return false;
        }

        brushCount++;
        if (brushCount >= REQUIRED_BRUSHES) {
            // 破坏方块并掉落本体
            BlockState state = getCachedState();
            world.breakBlock(pos, false);
            // 掉落自身物品
            ItemStack drop = new ItemStack(state.getBlock().asItem());
            ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
        }
        return true;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.brushCount = nbt.getInt("BrushCount");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("BrushCount", this.brushCount);
    }

    /**
     * 标记该焰晶晶簇是玩家放置的。
     */
    public void markPlayer() {
        isNatural = false;
    }

    public boolean isNatural() {
        return isNatural;
    }
}
