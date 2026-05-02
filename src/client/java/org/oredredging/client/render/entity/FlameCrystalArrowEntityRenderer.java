package org.oredredging.client.render.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.entity.FlameCrystalArrowEntity;

public class FlameCrystalArrowEntityRenderer extends ProjectileEntityRenderer<FlameCrystalArrowEntity> {
    public static final Identifier TEXTURE = new Identifier(OreDredging.MOD_ID, "textures/entity/projectiles/flame_crystal_arrow.png");

    public FlameCrystalArrowEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(FlameCrystalArrowEntity entity) {
        return TEXTURE;
    }
}
