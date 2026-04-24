package org.oredredging.registry;

public class RegistryInit {
    public static void init() {
        ModItems.registerAll();
        ModBlockEntities.registerAll();
        ModItemGroups.RegistryModItemGroups();
        ModEntities.registerAll();
        ModLootFunctionTypes.registerAll();
        ModBlockStateProviderTypes.registerAll();
        ModBiomeFeatures.registerAll();
        ModEnchantments.registerAll();
        ModLootPoolEntryTypes.registerAll();
        ModRecipeSerializers.registerAll();
        ModPlacementModifierTypes.registerAll();
        ModSoundEvent.registerAll();
        ModParticleTypes.registerAll();
    }
}
