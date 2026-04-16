package org.oredredging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.loot.ProbabilityItemEntry;
import org.oredredging.loot.TagEntry;

public class ModLootPoolEntryTypes {
    public static final DeferredRegister<LootPoolEntryType> LOOT_ENTRIES =
            DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, OreDredging.MOD_ID);

    public static final RegistryObject<LootPoolEntryType> PROBABILITY_ITEM =
            LOOT_ENTRIES.register("probability_item",
                    () -> new LootPoolEntryType(new ProbabilityItemEntry.Serializer()));

    public static final RegistryObject<LootPoolEntryType> TAG_ITEM =
            LOOT_ENTRIES.register("tag_item",
                    () -> new LootPoolEntryType(new TagEntry.Serializer()));

    public static void registerAll(IEventBus modEventBus) {
        LOOT_ENTRIES.register(modEventBus);
    }
}