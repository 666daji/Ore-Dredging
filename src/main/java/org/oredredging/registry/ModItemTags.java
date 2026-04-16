package org.oredredging.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.OreDredging;

public class ModItemTags {
    public static final TagKey<Item> PEBBLE = tag("pebble");
    public static final TagKey<Item> GRAVEL_PILES = tag("gravel_piles");
    public static final TagKey<Item> CIMELIA = tag("cimelia");

    private static TagKey<Item> tag(String id) {
        return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), OreDredging.createResourceLocation(OreDredging.MOD_ID, id));
    }
}