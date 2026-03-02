package org.oredredging.client.render;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import org.oredredging.client.render.model.ModModelLoader;
import org.oredredging.client.render.tooltip.MinerBundleTooltipComponent;
import org.oredredging.item.MinerBundleItem;

public class ModFabricEvents {
    public static void registryAll() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof MinerBundleItem.MinerBundleTooltipData minerData) {
                return new MinerBundleTooltipComponent(minerData);
            }
            return null;
        });

        ModelLoadingPlugin.register(new ModModelLoader());
    }
}
