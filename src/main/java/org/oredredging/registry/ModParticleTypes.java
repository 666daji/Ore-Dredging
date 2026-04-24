package org.oredredging.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModParticleTypes {
    public static final DefaultParticleType MIMETIC_LEY_LINE_DUST = register("mimetic_ley_line_dust", false);

    private static DefaultParticleType register(String name, boolean alwaysShow) {
        return Registry.register(Registries.PARTICLE_TYPE, name, FabricParticleTypes.simple(alwaysShow));
    }

    public static void registerAll() {}
}
