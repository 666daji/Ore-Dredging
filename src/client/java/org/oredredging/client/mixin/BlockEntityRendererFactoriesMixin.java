package org.oredredging.client.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import org.oredredging.client.render.block.MimeticLeyLineBlockEntityRenderer;
import org.oredredging.client.render.block.ThunderSmelterPipeBlockEntityRenderer;
import org.oredredging.registry.ModBlockEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRendererFactories.class)
public abstract class BlockEntityRendererFactoriesMixin {
    @Shadow
    public static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererFactory<T> factory) {}

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void registerBlockEntityRenderers(CallbackInfo ci) {
        register(ModBlockEntities.MIMETIC_LEY_LINE, MimeticLeyLineBlockEntityRenderer::new);
        register(ModBlockEntities.THUNDER_SMELTER_PIPE, ThunderSmelterPipeBlockEntityRenderer::new);
    }
}
