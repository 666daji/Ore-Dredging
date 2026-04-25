package org.oredredging.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;

public class ModSoundEvent {
    public static final SoundEvent PILES_FALL = registerSoundEvent("piles_fall");
    public static final SoundEvent HAMMER_HIT = registerSoundEvent("hammer_hit");

    // 拟态地脉
    public static final SoundEvent MLL_AMASS = registerSoundEvent("mll_amass");
    public static final SoundEvent MLL_BUDDING_RA = registerSoundEvent("mll_budding_ra");
    public static final SoundEvent MLL_BUDDING_SHOCK = registerSoundEvent("mll_budding_shock");
    public static final SoundEvent MLL_ERUPT = registerSoundEvent("mll_erupt");
    public static final SoundEvent MLL_ERUPT_SMOKE = registerSoundEvent("mll_erupt_smoke");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(OreDredging.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerAll() {}
}
