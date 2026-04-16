package org.oredredging.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.OreDredging;

public class ModBlockTags {
    public static final TagKey<Block> PEBBLE = tag("pebble");
    public static final TagKey<Block> GRAVEL_PILES = tag("gravel_piles");
    public static final TagKey<Block> GLASS_PANES = tag("glass_panes");
    public static final TagKey<Block> CAN_PEBBLE_BREAK_OTHER = tag("can_pebble_break_other");

    private static TagKey<Block> tag(String id) {
        return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), OreDredging.createResourceLocation(OreDredging.MOD_ID, id));
    }
}