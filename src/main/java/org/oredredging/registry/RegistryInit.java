package org.oredredging.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public class RegistryInit {
    public static void init(IEventBus modEventBus) {
        ModEntities.registerAll(modEventBus);
        ModBlocks.registerAll(modEventBus);
        ModItems.registerAll(modEventBus);
        ModItemGroups.registerAll(modEventBus);
        ModLootFunctionTypes.registerAll(modEventBus);
        ModBlockStateProviderTypes.registerAll(modEventBus);
        ModEnchantments.registerAll(modEventBus);
        ModLootPoolEntryTypes.registerAll(modEventBus);
        ModRecipeSerializers.registerAll(modEventBus);
        ModPlacementModifierTypes.registerAll(modEventBus);
        ModSoundEvent.registerAll(modEventBus);
    }
}
