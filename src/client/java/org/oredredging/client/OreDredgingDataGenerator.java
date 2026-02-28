package org.oredredging.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.oredredging.client.datagen.ModBlockLootTableGenerator;
import org.oredredging.client.datagen.ModRecipeGenerator;

public class OreDredgingDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModBlockLootTableGenerator::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<ModRecipeGenerator>) ModRecipeGenerator::new);
    }
}
