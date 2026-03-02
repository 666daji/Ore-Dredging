package org.oredredging.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModEnchantments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 表示{@link ModEnchantments#CONVERGENCE}效果允许的配方。
 *
 * <p>只允许{@linkplain ShapedRecipe}和{@linkplain ShapelessRecipe}</p>
 *
 * @see MinerBundleItem
 */
public record ConvergenceRecipesData(Set<Identifier> recipes) {
    public static final Codec<ConvergenceRecipesData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.listOf().fieldOf("recipes").forGetter(ConvergenceRecipesData::getRecipes)
            ).apply(instance, ConvergenceRecipesData::new)
    );

    public static final ConvergenceRecipesData DEFAULT = new ConvergenceRecipesData(Set.of(
            new Identifier("gold_ingot_from_nuggets"),
            new Identifier("iron_ingot_from_nuggets"),
            new Identifier(OreDredging.MOD_ID, "raw_gold_from_raw_gold_nugget"),
            new Identifier(OreDredging.MOD_ID, "raw_iron_from_raw_iron_nugget"),
            new Identifier(OreDredging.MOD_ID, "raw_copper_from_raw_copper_nugget")
    ));

    private List<Identifier> getRecipes() {
        return new ArrayList<>(recipes);
    }

    private ConvergenceRecipesData(List<Identifier> recipes) {
        this(new HashSet<>(recipes));
    }
}