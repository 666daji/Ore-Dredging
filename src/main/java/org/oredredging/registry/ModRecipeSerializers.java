package org.oredredging.registry;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.recipe.SmithingEnchantmentRecipe;
import org.oredredging.recipe.SmithingEnchantmentUpgradesRecipe;

public class ModRecipeSerializers {
    public static final RecipeSerializer<?> SMITHING_ENCHANTMENT = register("smithing_enchantment", new SmithingEnchantmentRecipe.Serializer());
    public static final RecipeSerializer<?> SMITHING_ENCHANTMENT_UPGRADES = register("smithing_enchantment_upgrades", new SmithingEnchantmentUpgradesRecipe.Serializer());

    private static <S extends RecipeSerializer<?>> S register(String id, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(OreDredging.MOD_ID, id), serializer);
    }

    public static void registerAll() {}
}