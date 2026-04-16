package org.oredredging.mixin.client;

import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
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
    private ItemModelShaper itemModelShaper;

    @ModifyVariable(
            method = "render",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BakedModel renderFlourItem(
            BakedModel originalModel,
            ItemStack stack) {
        if (stack.getItem() instanceof MinerBundleItem minerBundle && !MinerBundleItem.isEmpty(stack)) {
            ResourceLocation replaceModel = ModModelLoader.createItemModel(ForgeRegistries.ITEMS.getKey(minerBundle).getPath() + "_close");
            return this.itemModelShaper.getModelManager().getModel(replaceModel);
        }

        return originalModel;
    }
}
