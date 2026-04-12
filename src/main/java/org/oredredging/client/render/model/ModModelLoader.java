package org.oredredging.client.render.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.registry.ModItems;

import java.util.HashSet;
import java.util.Set;

public class ModModelLoader implements ModelLoadingPlugin {
    public static final Set<Identifier> MODELS_TO_LOAD = new HashSet<>();

    public static final Set<Item> ALL_MINER_BUNDLE = Set.of(ModItems.LEATHER_MINER_BUNDLE, ModItems.CHAIN_MINER_BUNDLE, ModItems.PHANTOM_MINER_BUNDLE);

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        registerAllMinerBundleModels();

        pluginContext.addModels(MODELS_TO_LOAD);
    }

    private void registerAllMinerBundleModels() {
        for (Item minerBundle : ALL_MINER_BUNDLE) {
            Identifier itemId = Registries.ITEM.getId(minerBundle);

            String minerBundleModelName = itemId.getPath() + "_close";
            ModelIdentifier modelId = createItemModel(minerBundleModelName);
            MODELS_TO_LOAD.add(modelId);
        }
    }

    public static ModelIdentifier createItemModel(String itemPath) {
        return new ModelIdentifier(new Identifier(OreDredging.MOD_ID, itemPath), "inventory");
    }
}
