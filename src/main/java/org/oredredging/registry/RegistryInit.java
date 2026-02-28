package org.oredredging.registry;

import org.oredredging.util.DropUtil;

public class RegistryInit {
    public static void init() {
        ModItems.registerAll();
        ModItemGroups.RegistryModItemGroups();
        ModLootFunctionTypes.registerAll();
        ModBlockStateProviderTypes.registerAll();
        ModBiomeFeatures.registerAll();

        DropUtil.reload();
    }
}
