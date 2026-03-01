package org.oredredging.config;

import org.oredredging.config.framework.ConfigManager;
import org.oredredging.config.framework.ConfigType;

public final class ModConfigs {
    public static final ConfigType<CrushedDropsData> CRUSHED_DROPS = ConfigType.of(
            "crushedDrops",
            CrushedDropsData.CODEC,
            CrushedDropsData.DEFAULT,
            CrushedDropsData.migrator()
    );

    public static final ConfigType<BundlesData> BUNDLES = ConfigType.of(
            "bundleAllowItems",
            BundlesData.CODEC,
            BundlesData.DEFAULT,
            null
    );

    public static void registerAll() {
        ConfigManager.register(CRUSHED_DROPS);
        ConfigManager.register(BUNDLES);

        ConfigManager.loadAll();
    }
}