package org.oredredging.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class MimeticLeyLineDustParticle extends SpriteBillboardParticle {
    private static final int SUBDIVISIONS = 8;        // 16x16 纹理分为 8x8 个区域 = 2x2 像素/区域
    private static final float PIXEL_SIZE = 16.0F / SUBDIVISIONS;  // = 2.0F
    private final int uIndex;
    private final int vIndex;

    public MimeticLeyLineDustParticle(ClientWorld world, double x, double y, double z,
                                      double vx, double vy, double vz) {
        super(world, x, y, z, vx, vy, vz);
        this.setSprite(MinecraftClient.getInstance()
                .getBlockRenderManager()
                .getModels()
                .getModelParticleSprite(Blocks.DEEPSLATE.getDefaultState()));

        Random rand = this.random;
        uIndex = rand.nextInt(SUBDIVISIONS);
        vIndex = rand.nextInt(SUBDIVISIONS);

        this.scale = 0.03f;
        this.gravityStrength = 0.05f;
        this.velocityMultiplier = 0.99f;
        this.maxAge = 50 + rand.nextInt(100);

        this.velocityX = vx * 0.4 + (rand.nextDouble() - 0.5) * 0.02;
        this.velocityY = vy * 0.3 + rand.nextDouble() * 0.02;
        this.velocityZ = vz * 0.4 + (rand.nextDouble() - 0.5) * 0.02;

        float brightness = 0.5f + rand.nextFloat() * 0.3f;
        this.red = brightness;
        this.green = brightness;
        this.blue = brightness;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getFrameU((uIndex + 1) * PIXEL_SIZE);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getFrameU(uIndex * PIXEL_SIZE);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getFrameV((vIndex + 1) * PIXEL_SIZE);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getFrameV(vIndex * PIXEL_SIZE);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.TERRAIN_SHEET;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new MimeticLeyLineDustParticle(world, x, y, z, vx, vy, vz);
        }
    }
}