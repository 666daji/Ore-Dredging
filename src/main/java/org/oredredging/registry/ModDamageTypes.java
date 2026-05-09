package org.oredredging.registry;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;

public class ModDamageTypes {
    public static final RegistryKey<DamageType> PEBBLE_HIT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(OreDredging.MOD_ID, "pebble_hit"));
    public static final RegistryKey<DamageType> FLAME_CRYSTAL_RED_STONE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(OreDredging.MOD_ID, "flame_crystal_red_stone"));
}
