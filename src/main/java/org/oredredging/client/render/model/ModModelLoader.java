package org.oredredging.client.render.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.OreDredging;
import org.oredredging.registry.ModItems;

import java.util.HashSet;
import java.util.Set;

public class ModModelLoader{
    public static final Set<ResourceLocation> MODELS_TO_LOAD = new HashSet<>();

    public static final Set<Item> ALL_MINER_BUNDLE = Set.of(ModItems.LEATHER_MINER_BUNDLE.get(), ModItems.CHAIN_MINER_BUNDLE.get(), ModItems.PHANTOM_MINER_BUNDLE.get());

    public static void initModels(ModelEvent.RegisterAdditional event) {
        registerAllMinerBundleModels();

        for (ResourceLocation modelId : MODELS_TO_LOAD) {
            event.register(modelId);
        }
    }

    private static void registerAllMinerBundleModels() {
        for (Item minerBundle : ALL_MINER_BUNDLE) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(minerBundle);

            if (itemId == null) {
                return;
            }

            String minerBundleModelName = itemId.getPath() + "_close";
            ModelResourceLocation modelId = createItemModel(minerBundleModelName);
            MODELS_TO_LOAD.add(modelId);
        }
    }

    public static ModelResourceLocation createItemModel(String itemPath) {
        return new ModelResourceLocation(OreDredging.createResourceLocation(OreDredging.MOD_ID, itemPath), "inventory");
    }
}
