package org.oredredging.client.mixin;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.oredredging.client.render.model.ModModelLoader;
import org.oredredging.item.MinerBundleItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Shadow
    @Final
    private ItemModels models;

    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BakedModel renderFlourItem(
            BakedModel originalModel,
            ItemStack stack) {
        if (stack.getItem() instanceof MinerBundleItem minerBundle && !MinerBundleItem.isEmpty(stack)) {
            Identifier replaceModel = ModModelLoader.createItemModel(Registries.ITEM.getId(minerBundle).getPath() + "_close");
            return this.models.getModelManager().getModel(replaceModel);
        }

        return originalModel;
    }
}
