package org.oredredging.client.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.oredredging.item.CollapseStoneHammerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;"
            )
    )
    private void onRenderFirstPersonItem(
            AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        // 仅对锤子物品生效
        if (!(stack.getItem() instanceof CollapseStoneHammerItem)) return;

        boolean bl = hand == Hand.MAIN_HAND;
        Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();

        // 判断玩家是否正在使用该物品（蓄力）
        if (player.isUsingItem() && player.getActiveHand() == hand) {
            // 计算蓄力进度（基于已使用时间）
            int maxUseTime = stack.getMaxUseTime();
            int timeLeft = player.getItemUseTimeLeft();
            int usedTime = maxUseTime - timeLeft;
            float progress = MathHelper.clamp(usedTime / 20.0F, 0.0F, 1.0F);

            // 根据左右手调整方向
            float direction = (arm == Arm.RIGHT) ? 1.0F : -1.0F;

            // 应用动画变换：上下移动和绕 X 轴旋转，模拟挥动
            matrices.translate(0.0F, -1F, 0F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(progress * 30.0F * direction));
            matrices.translate(0.0F, 1F, 0F);
        }
    }
}