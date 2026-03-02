package org.oredredging.registry;

public class RegistryInit {
    public static void init() {
        ModItems.registerAll();
        ModItemGroups.RegistryModItemGroups();
        ModLootFunctionTypes.registerAll();
        ModBlockStateProviderTypes.registerAll();
        ModBiomeFeatures.registerAll();
        ModEnchantments.registerAll();
    }
}
