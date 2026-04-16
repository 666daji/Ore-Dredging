package org.oredredging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import org.oredredging.OreDredging;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> PEBBLE_HIT = ResourceKey.create(Registries.DAMAGE_TYPE,
            OreDredging.createResourceLocation(OreDredging.MOD_ID, "pebble_hit"));
}