package org.oredredging.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.entity.*;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OreDredging.MOD_ID);

    public static final RegistryObject<EntityType<PebbleEntity>> PEBBLE = register("pebble",
            () -> EntityType.Builder.<PebbleEntity>of(PebbleEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .setTrackingRange(100)
                    .setUpdateInterval(40)
                    .build("pebble"));

    public static final RegistryObject<EntityType<DetonatorEntity>> DETONATOR = register("detonator",
            () -> EntityType.Builder.<DetonatorEntity>of(DetonatorEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .setTrackingRange(100)
                    .setUpdateInterval(40)
                    .build("detonator"));

    public static final RegistryObject<EntityType<SlimyDetonatorEntity>> SLIMY_DETONATOR = register("slimy_detonator",
            () -> EntityType.Builder.<SlimyDetonatorEntity>of(SlimyDetonatorEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .setTrackingRange(100)
                    .setUpdateInterval(40)
                    .build("slimy_detonator"));

    public static final RegistryObject<EntityType<ImpactDetonatorEntity>> IMPACT_DETONATOR = register("impact_detonator",
            () -> EntityType.Builder.<ImpactDetonatorEntity>of(ImpactDetonatorEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .setTrackingRange(100)
                    .setUpdateInterval(40)
                    .build("impact_detonator"));

    private static <T extends EntityType<?>> RegistryObject<T> register(String id, Supplier<T> supplier) {
        return ENTITY_TYPES.register(id, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}