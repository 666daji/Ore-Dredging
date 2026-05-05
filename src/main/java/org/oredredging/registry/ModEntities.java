package org.oredredging.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.entity.*;

public class ModEntities {
    public static final EntityType<PebbleEntity> PEBBLE = register("pebble", FabricEntityTypeBuilder.<PebbleEntity>create(SpawnGroup.MISC, (PebbleEntity::new))
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(40)
            .trackRangeChunks(10)
            .build());

    // 雷管
    public static final EntityType<DetonatorEntity> DETONATOR = register("detonator", FabricEntityTypeBuilder.<DetonatorEntity>create(SpawnGroup.MISC, DetonatorEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(40)
            .trackRangeChunks(10)
            .build());
    public static final EntityType<SlimyDetonatorEntity> SLIMY_DETONATOR = register("slimy_detonator", FabricEntityTypeBuilder.<SlimyDetonatorEntity>create(SpawnGroup.MISC, SlimyDetonatorEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(40)
            .trackRangeChunks(10)
            .build());
    public static final EntityType<ImpactDetonatorEntity> IMPACT_DETONATOR = register("impact_detonator", FabricEntityTypeBuilder.<ImpactDetonatorEntity>create(SpawnGroup.MISC, ImpactDetonatorEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(40)
            .trackRangeChunks(10)
            .build());

    // 焰晶
    public static final EntityType<FlameCrystalEntity> FLAME_CRYSTAL = register("flame_crystal", FabricEntityTypeBuilder.<FlameCrystalEntity>create(SpawnGroup.MISC, FlameCrystalEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(40)
            .trackRangeChunks(10)
            .build());
    public static final EntityType<FlameCrystalArrowEntity> FLAME_CRYSTAL_ARROW = register("flame_crystal_arrow", FabricEntityTypeBuilder.<FlameCrystalArrowEntity>create(SpawnGroup.MISC, FlameCrystalArrowEntity::new)
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(2)
            .trackedUpdateRate(4)
            .trackRangeChunks(10)
            .build());

    public static final EntityType<MimeticLeyLineFallingEntity> MIMETIC_LEY_LINE_FALLING = register("mimetic_ley_line_falling", FabricEntityTypeBuilder.<MimeticLeyLineFallingEntity>create(SpawnGroup.MISC, MimeticLeyLineFallingEntity::new)
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(2)
            .trackedUpdateRate(4)
            .trackRangeChunks(10)
            .build());
    public static final EntityType<ThermalCloudEntity> THERMAL_CLOUD = register("thermal_cloud",  FabricEntityTypeBuilder.<ThermalCloudEntity>create(SpawnGroup.MISC, ThermalCloudEntity::new)
            .dimensions(EntityDimensions.fixed(10F, 10F))
            .trackRangeChunks(10)
            .build());

    private static <T extends Entity> EntityType<T> register(String id, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(OreDredging.MOD_ID, id), type);
    }

    public static void registerAll() {}
}
