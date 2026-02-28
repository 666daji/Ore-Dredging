package org.oredredging.registry;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import org.oredredging.OreDredging;
import org.oredredging.feature.RandomGravelPilesStateProvider;

public class ModBlockStateProviderTypes {
    public static final BlockStateProviderType<RandomGravelPilesStateProvider> RANDOM_GRAVEL_PILES =
            register("random_gravel_piles", RandomGravelPilesStateProvider.CODEC);

    private static <P extends BlockStateProvider> BlockStateProviderType<P> register(String id, Codec<P> codec) {
        return Registry.register(Registries.BLOCK_STATE_PROVIDER_TYPE, new Identifier(OreDredging.MOD_ID, id), new BlockStateProviderType<>(codec));
    }

    public static void registerAll() {}
}
