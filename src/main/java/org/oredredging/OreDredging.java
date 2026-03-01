package org.oredredging;

import net.fabricmc.api.ModInitializer;
import org.oredredging.config.ModConfigs;
import org.oredredging.registry.RegistryInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OreDredging implements ModInitializer {
    public static final String MOD_ID = "ore_dredging";
    public static final Logger LOGGER = LoggerFactory.getLogger("Tw`s Ore Dredging");

    @Override
    public void onInitialize() {
        RegistryInit.init();
        ModConfigs.registerAll();
        LOGGER.info("Tw`s Ore Dredging is initializing!");
    }
}
