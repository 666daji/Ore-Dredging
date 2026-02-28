package org.oredredging.registry;

import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.loot.CrushedDropCountFunction;

public class ModLootFunctionTypes {
    public static final LootFunctionType CRUSHED_DROP_COUNT = register("crushed_drop", new LootFunctionType(new CrushedDropCountFunction.Serializer()));

    public static LootFunctionType register(String id, LootFunctionType type) {
        return Registry.register(Registries.LOOT_FUNCTION_TYPE,
                new Identifier(OreDredging.MOD_ID, id), type);
    }

    public static void registerAll() {}
}
