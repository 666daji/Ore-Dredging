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
import org.oredredging.entity.PebbleEntity;

public class ModEntities {
    public static final EntityType<PebbleEntity> PEBBLE = register("pebble", FabricEntityTypeBuilder.<PebbleEntity>create(SpawnGroup.MISC, (PebbleEntity::new))
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(100)
            .trackedUpdateRate(40)
            .build());

    private static <T extends Entity> EntityType<T> register(String id, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(OreDredging.MOD_ID, id), type);
    }

    public static void registerAll() {}
}
