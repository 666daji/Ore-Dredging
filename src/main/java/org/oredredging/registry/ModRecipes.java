package org.oredredging.registry;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.recipe.ThunderSmeltRecipe;

public class ModRecipes {
    public static final RecipeType<ThunderSmeltRecipe> THUNDER_SMELT = register("thunder_smelt");

    static <T extends Recipe<?>> RecipeType<T> register(String id) {
        return Registry.register(Registries.RECIPE_TYPE, new Identifier(OreDredging.MOD_ID, id), new RecipeType<T>() {
            public String toString() {
                return id;
            }
        });
    }

    public static void registerAll() {}
}
