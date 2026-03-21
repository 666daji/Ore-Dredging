package org.oredredging.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.entity.*;

public class ModEntities {
    public static final EntityType<PebbleEntity> PEBBLE = register("pebble", FabricEntityTypeBuilder.<PebbleEntity>create(SpawnGroup.MISC, (PebbleEntity::new))
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(100)
            .trackedUpdateRate(40)
            .build());

    // 雷管
    public static final EntityType<DetonatorEntity> DETONATOR = register("detonator", FabricEntityTypeBuilder.<DetonatorEntity>create(SpawnGroup.MISC, DetonatorEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(100)
            .trackedUpdateRate(40)
            .build());
    public static final EntityType<SlimyDetonatorEntity> SLIMY_DETONATOR = register("slimy_detonator", FabricEntityTypeBuilder.<SlimyDetonatorEntity>create(SpawnGroup.MISC, SlimyDetonatorEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(100)
            .trackedUpdateRate(40)
            .build());
    public static final EntityType<ImpactDetonatorEntity> IMPACT_DETONATOR = register("impact_detonator", FabricEntityTypeBuilder.<ImpactDetonatorEntity>create(SpawnGroup.MISC, ImpactDetonatorEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(100)
            .trackedUpdateRate(40)
            .build());

    private static <T extends Entity> EntityType<T> register(String id, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(OreDredging.MOD_ID, id), type);
    }

    public static void registerAll() {}
}
