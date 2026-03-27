package org.oredredging.registry;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;
import org.oredredging.OreDredging;
import org.oredredging.feature.ColumnScanPlacementModifier;

public class ModPlacementModifierTypes {
    public static final PlacementModifierType<ColumnScanPlacementModifier> COLUMN_SCAN = register("column_scan", ColumnScanPlacementModifier.MODIFIER_CODEC);

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String id, Codec<P> codec) {
        return Registry.register(Registries.PLACEMENT_MODIFIER_TYPE, new Identifier(OreDredging.MOD_ID, id), () -> codec);
    }

    public static void registerAll() {}
}
