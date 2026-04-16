package org.oredredging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.feature.RandomGravelPilesStateProvider;

public class ModBlockStateProviderTypes {
    public static final DeferredRegister<BlockStateProviderType<?>> PROVIDER_TYPES =
            DeferredRegister.create(Registries.BLOCK_STATE_PROVIDER_TYPE, OreDredging.MOD_ID);

    public static final RegistryObject<BlockStateProviderType<RandomGravelPilesStateProvider>> RANDOM_GRAVEL_PILES =
            PROVIDER_TYPES.register("random_gravel_piles",
                    () -> new BlockStateProviderType<>(RandomGravelPilesStateProvider.CODEC));

    public static void registerAll(IEventBus modEventBus) {
        PROVIDER_TYPES.register(modEventBus);
    }
}