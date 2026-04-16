package org.oredredging.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.recipe.SmithingEnchantmentRecipe;
import org.oredredging.recipe.SmithingEnchantmentUpgradesRecipe;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, OreDredging.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SmithingEnchantmentRecipe>> SMITHING_ENCHANTMENT =
            RECIPE_SERIALIZERS.register("smithing_enchantment",
                    SmithingEnchantmentRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<SmithingEnchantmentUpgradesRecipe>> SMITHING_ENCHANTMENT_UPGRADES =
            RECIPE_SERIALIZERS.register("smithing_enchantment_upgrades",
                    SmithingEnchantmentUpgradesRecipe.Serializer::new);

    public static void registerAll(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}