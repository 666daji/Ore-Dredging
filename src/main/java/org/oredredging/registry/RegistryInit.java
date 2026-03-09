package org.oredredging.registry;

public class RegistryInit {
    public static void init() {
        ModItems.registerAll();
        ModItemGroups.RegistryModItemGroups();
        ModEntities.registerAll();
        ModLootFunctionTypes.registerAll();
        ModBlockStateProviderTypes.registerAll();
        ModBiomeFeatures.registerAll();
        ModEnchantments.registerAll();
        ModLootPoolEntryTypes.registerAll();
        ModRecipeSerializers.registerAll();
        ModSoundEvent.registerAll();
    }
}
