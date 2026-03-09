package org.oredredging.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;

public class ModSoundEvent {
    public static final SoundEvent PILES_FALL = registerSoundEvent("piles_fall");
    public static final SoundEvent PEBBLE_BREAK = registerSoundEvent("pebble_break");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(OreDredging.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerAll() {}
}
