package org.oredredging.client.render.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class ModModelLoader implements ModelLoadingPlugin {
    public static final Set<Identifier> LOAD_MODELS = new HashSet<>();

    @Override
    public void onInitializeModelLoader(Context pluginContext) {


        pluginContext.addModels(LOAD_MODELS);
    }
}
