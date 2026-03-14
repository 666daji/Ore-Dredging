package org.oredredging.registry;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;

public class ModBlockTags {
    public static TagKey<Block> PEBBLE = of("pebble");
    public static TagKey<Block> GRAVEL_PILES = of("gravel_piles");
    public static TagKey<Block> GLASS_PANES = of("glass_panes");
    public static TagKey<Block> CAN_PEBBLE_BREAK_OTHER = of("can_pebble_break_other");

    private static TagKey<Block> of(String id) {
        return TagKey.of(RegistryKeys.BLOCK, new Identifier(OreDredging.MOD_ID, id));
    }
}
