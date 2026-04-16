package org.oredredging.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;

public class ModSoundEvent {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, OreDredging.MOD_ID);

    public static final RegistryObject<SoundEvent> PILES_FALL = register("piles_fall");
    public static final RegistryObject<SoundEvent> HAMMER_HIT = register("hammer_hit");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(OreDredging.createResourceLocation(OreDredging.MOD_ID, name)));
    }

    public static void registerAll(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}