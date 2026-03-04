package org.oredredging.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;

public class ModItemTags {
    public static TagKey<Item> PEBBLE = of("pebble");
    public static TagKey<Item> GRAVEL_PILES = of("gravel_piles");

    private static TagKey<Item> of(String id) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(OreDredging.MOD_ID, id));
    }
}
