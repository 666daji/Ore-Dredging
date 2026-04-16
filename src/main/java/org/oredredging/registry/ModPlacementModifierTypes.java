package org.oredredging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.feature.ColumnScanPlacementModifier;

public class ModPlacementModifierTypes {
    public static final DeferredRegister<PlacementModifierType<?>> MODIFIER_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, OreDredging.MOD_ID);

    public static final RegistryObject<PlacementModifierType<ColumnScanPlacementModifier>> COLUMN_SCAN =
            MODIFIER_TYPES.register("column_scan",
                    () -> () -> ColumnScanPlacementModifier.MODIFIER_CODEC);

    public static void registerAll(IEventBus modEventBus) {
        MODIFIER_TYPES.register(modEventBus);
    }
}