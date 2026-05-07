package org.oredredging.registry;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.block.entity.*;

public class ModBlockEntities {
    public static final BlockEntityType<MimeticLeyLineBlockEntity> MIMETIC_LEY_LINE = create("mimetic_ley_line",
            BlockEntityType.Builder.create(MimeticLeyLineBlockEntity::new, ModBlocks.MIMETIC_LEY_LINE));
    public static final BlockEntityType<ThunderSmelterPipeBlockEntity> THUNDER_SMELTER_PIPE = create("thunder_smelter_pipe",
            BlockEntityType.Builder.create(ThunderSmelterPipeBlockEntity::new, ModBlocks.THUNDER_SMELTER_PIPE));
    public static final BlockEntityType<FlameCrystalClusterBlockEntity> FLAME_CRYSTAL_CLUSTER = create("flame_crystal_cluster",
            BlockEntityType.Builder.create(FlameCrystalClusterBlockEntity::new, ModBlocks.FLAME_CRYSTAL_CLUSTER));

    private static <T extends BlockEntity> BlockEntityType<T> create(String id, BlockEntityType.Builder<T> builder) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(OreDredging.MOD_ID, id), builder.build(null));
    }

    public static void registerAll() {}
}
