package org.oredredging.registry;

import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.loot.ProbabilityItemEntry;
import org.oredredging.loot.TagEntry;

public class ModLootPoolEntryTypes {
    public static final LootPoolEntryType PROBABILITY_ITEM = register("probability_item", new LootPoolEntryType(new ProbabilityItemEntry.Serializer())) ;
    public static final LootPoolEntryType TAG_ITEM = register("tag_item", new LootPoolEntryType(new TagEntry.Serializer()));

    private static LootPoolEntryType register(String id, LootPoolEntryType entryType) {
        return Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, new Identifier(OreDredging.MOD_ID, id), entryType);
    }

    public static void registerAll() {}
}
