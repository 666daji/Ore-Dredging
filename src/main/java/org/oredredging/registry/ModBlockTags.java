package org.oredredging.registry;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;

public class ModBlockTags {
    public static TagKey<Block> PEBBLE = of("pebble");
    public static TagKey<Block> GRAVEL_PILES = of("gravel_piles");

    private static TagKey<Block> of(String id) {
        return TagKey.of(RegistryKeys.BLOCK, new Identifier(OreDredging.MOD_ID, id));
    }
}
