package org.oredredging.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.oredredging.block.entity.MimeticLeyLineBlockEntity;
import org.oredredging.registry.ModBlocks;
import org.oredredging.registry.ModEntities;

/**
 * 拟态地脉专用坠落实体，无伤害、无加速度、匀速下落。
 * 携带方块实体完整 NBT，可随时因下方磁石恢复而重新变为方块。
 */
public class MimeticLeyLineFallingEntity extends Entity {
    private BlockState blockState = ModBlocks.MIMETIC_LEY_LINE.getDefaultState();
    private NbtCompound blockEntityData;

    private static final int MAGNET_CHECK_RADIUS = 4;
    private static final int MAGNET_CHECK_INTERVAL = 5;

    public MimeticLeyLineFallingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public MimeticLeyLineFallingEntity(World world, double x, double y, double z,
                                       BlockState blockState, NbtCompound entityNbt) {
        super(ModEntities.MIMETIC_LEY_LINE_FALLING, world);
        this.blockState = blockState;
        this.blockEntityData = entityNbt;
        this.setPosition(x, y, z);
        this.setVelocity(Vec3d.ZERO);
        this.intersectionChecked = true;
    }

    @Override
    protected void initDataTracker() {}

    @Override
    public void tick() {
        super.tick();

        // 匀速下落
        this.setVelocity(new Vec3d(0, -MimeticLeyLineBlockEntity.FALL_SPEED, 0));
        this.move(MovementType.SELF, this.getVelocity());

        if (!getWorld().isClient()) {
            BlockPos currentPos = this.getBlockPos();

            // 定期检测磁石，恢复为方块
            if (age % MAGNET_CHECK_INTERVAL == 0 && hasMagnetBelow()) {
                restoreToBlock(currentPos);
                return;
            }

            // 落地恢复
            if (isOnGround()) {
                restoreToBlock(currentPos);
            }

            // 超时保护
            if (age > 600 || currentPos.getY() <= this.getWorld().getBottomY()) {
                this.discard();
            }
        }
    }

    private boolean hasMagnetBelow() {
        BlockPos.Mutable mutable = getBlockPos().mutableCopy();
        for (int i = 1; i <= MAGNET_CHECK_RADIUS; i++) {
            mutable.move(0, -i, 0);
            if (this.getWorld().getBlockState(mutable).isOf(Blocks.LODESTONE)) {
                return true;
            }
        }
        return false;
    }

    private void restoreToBlock(BlockPos pos) {
        MimeticLeyLineBlockEntity.placeFromFalling(
                this.getWorld(), pos, this.blockState, this.blockEntityData
        );
        this.discard();
    }

    // ================== 无伤害 ==================
    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    // ================== 数据持久化 ==================
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.blockState = NbtHelper.toBlockState(
                this.getWorld().createCommandRegistryWrapper(net.minecraft.registry.RegistryKeys.BLOCK),
                nbt.getCompound("BlockState")
        );
        if (nbt.contains("BlockEntityData")) {
            this.blockEntityData = nbt.getCompound("BlockEntityData");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.put("BlockState", NbtHelper.fromBlockState(this.blockState));
        if (this.blockEntityData != null) {
            nbt.put("BlockEntityData", this.blockEntityData);
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this, net.minecraft.block.Block.getRawIdFromState(this.blockState));
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.blockState = net.minecraft.block.Block.getStateFromRawId(packet.getEntityData());
        this.intersectionChecked = true;
        this.setPosition(packet.getX(), packet.getY(), packet.getZ());
    }
}