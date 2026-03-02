package org.oredredging.client;

import net.fabricmc.api.ClientModInitializer;
import org.oredredging.client.render.ModFabricEvents;

public class OreDredgingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModFabricEvents.registryAll();
    }
}
