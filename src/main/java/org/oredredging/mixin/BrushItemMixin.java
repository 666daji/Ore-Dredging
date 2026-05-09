package org.oredredging.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BrushItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.oredredging.block.entity.FlameCrystalClusterBlockEntity;
import org.oredredging.registry.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushItem.class)
public abstract class BrushItemMixin {

    @Shadow
    protected abstract HitResult getHitResult(LivingEntity user);

    @Inject(method = "usageTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;)V"))
    private void canBrushExtra(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        BlockHitResult hitResult = (BlockHitResult) getHitResult(user);

        if (!world.isClient() && world.getBlockEntity(hitResult.getBlockPos()) instanceof FlameCrystalClusterBlockEntity flameCrystalClusterBlockEntity) {
            boolean bl2 = flameCrystalClusterBlockEntity.brush(world.getTime(), (PlayerEntity) user, hitResult.getSide());
            if (bl2) {
                EquipmentSlot equipmentSlot = stack.equals(user.getEquippedStack(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                stack.damage(1, user, userX -> userX.sendEquipmentBreakStatus(equipmentSlot));
            }
        }
    }

    @ModifyVariable(method = "addDustParticles", at = @At("HEAD"), argsOnly = true, index = 3)
    private BlockState modifyParticle(BlockState state) {
        if (state.isOf(ModBlocks.FLAME_CRYSTAL_CLUSTER)) {
            return Blocks.GLOWSTONE.getDefaultState();
        }

        return state;
    }
}
