package org.oredredging.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.oredredging.item.Wave;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"
            )
    )
    private void onRenderFirstPersonItem(
            AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        if (!(stack.getItem() instanceof Wave)) return;

        boolean bl = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();

        // 判断玩家是否正在使用该物品（蓄力）
        if (player.isUsingItem() && player.getUsedItemHand() == hand) {
            // 计算蓄力进度（基于已使用时间）
            int maxUseTime = stack.getUseDuration();
            int timeLeft = player.getUseItemRemainingTicks();
            int usedTime = maxUseTime - timeLeft;
            float progress = Mth.clamp(usedTime / 20.0F, 0.0F, 1.0F);

            // 根据左右手调整方向
            float direction = (arm == HumanoidArm.RIGHT) ? 1.0F : -1.0F;

            // 应用动画变换：上下移动和绕 X 轴旋转，模拟挥动
            matrices.translate(0.0F, -1F, 0F);
            matrices.mulPose(Axis.XP.rotationDegrees(progress * 30.0F * direction));
            matrices.translate(0.0F, 1F, 0F);
        }
    }
}