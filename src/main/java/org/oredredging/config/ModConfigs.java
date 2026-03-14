package org.oredredging.config;

import org.oredredging.config.framework.ConfigManager;
import org.oredredging.config.framework.ConfigType;

public final class ModConfigs {
    public static final ConfigType<CrushedDropsData> CRUSHED_DROPS = ConfigType.of(
            "CrushedDrops",
            CrushedDropsData.CODEC,
            CrushedDropsData.DEFAULT,
            CrushedDropsData.migrator()
    );

    public static final ConfigType<BundlesData> BUNDLES = ConfigType.of(
            "BundleAllowItems",
            BundlesData.CODEC,
            BundlesData.DEFAULT,
            null
    );

    public static final ConfigType<ConvergenceRecipesData> CONVERGENCE_RECIPES = ConfigType.of(
            "ConvergenceRecipes",
            ConvergenceRecipesData.CODEC,
            ConvergenceRecipesData.DEFAULT,
            null
    );

    public static final ConfigType<CanPebbleBreakData> CAN_PEBBLE_BREAK = ConfigType.of(
            "CanPebbleBreak",
            CanPebbleBreakData.CODEC,
            CanPebbleBreakData.DEFAULT,
            null
    );

    public static void registerAll() {
        ConfigManager.register(CRUSHED_DROPS);
        ConfigManager.register(BUNDLES);
        ConfigManager.register(CONVERGENCE_RECIPES);
        ConfigManager.register(CAN_PEBBLE_BREAK);

        ConfigManager.loadAll();
    }
}