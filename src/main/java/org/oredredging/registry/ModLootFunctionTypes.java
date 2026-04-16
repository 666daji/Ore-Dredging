package org.oredredging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.loot.CrushedDropCountFunction;

public class ModLootFunctionTypes {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, OreDredging.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> CRUSHED_DROP_COUNT =
            LOOT_FUNCTIONS.register("crushed_drop",
                    () -> new LootItemFunctionType(new CrushedDropCountFunction.Serializer()));

    public static void registerAll(IEventBus modEventBus) {
        LOOT_FUNCTIONS.register(modEventBus);
    }
}