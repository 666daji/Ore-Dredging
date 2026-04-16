package org.oredredging.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.oredredging.OreDredging;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModEnchantments;

import java.util.*;

/**
 * 表示{@link ModEnchantments#CONVERGENCE}效果允许的配方。
 *
 * <p>只允许{@linkplain ShapedRecipe}和{@linkplain ShapelessRecipe}</p>
 *
 * @see MinerBundleItem
 */
public record ConvergenceRecipesData(Set<ResourceLocation> recipes) {
    public static final Codec<ConvergenceRecipesData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("recipes").forGetter(ConvergenceRecipesData::getRecipes)
            ).apply(instance, ConvergenceRecipesData::new)
    );

    /**
     * 缓存的配方信息。
     */
    private static List<RecipeInfo> cachedRecipeInfos = new ArrayList<>();

    public static final ConvergenceRecipesData DEFAULT = new ConvergenceRecipesData(Set.of(
            Objects.requireNonNull(ResourceLocation.tryParse("gold_ingot_from_nuggets")),
            Objects.requireNonNull(ResourceLocation.tryParse("iron_ingot_from_nuggets")),
            OreDredging.createResourceLocation(OreDredging.MOD_ID, "raw_gold_from_raw_gold_nugget"),
            OreDredging.createResourceLocation(OreDredging.MOD_ID, "raw_iron_from_raw_iron_nugget"),
            OreDredging.createResourceLocation(OreDredging.MOD_ID, "raw_copper_from_raw_copper_nugget")
    ));

    /**
     * 获取所有允许的配方信息。
     *
     * @param world 世界
     * @return 允许的配方信息列表
     */
    public List<RecipeInfo> getRecipeInfos(Level world) {
        // 如果缓存有效，直接返回
        if (cachedRecipeInfos != null && !cachedRecipeInfos.isEmpty()) {
            return cachedRecipeInfos;
        }

        // 重新解析配方
        List<RecipeInfo> infos = new ArrayList<>();
        RecipeManager recipeManager = world.getRecipeManager();
        for (ResourceLocation id : recipes()) {
            Optional<? extends Recipe<?>> optional = recipeManager.byKey(id);
            if (optional.isPresent()) {
                Recipe<?> recipe = optional.get();
                if (recipe instanceof CraftingRecipe craftingRecipe &&
                        (craftingRecipe instanceof ShapedRecipe || craftingRecipe instanceof ShapelessRecipe)) {
                    RecipeInfo info = RecipeInfo.fromCraftingRecipe(craftingRecipe, world.registryAccess());
                    if (info != null) {
                        infos.add(info);
                    }
                } else {
                    OreDredging.LOGGER.warn("Convergence recipe {} is not a shaped or shapeless crafting recipe, ignored", id);
                }
            } else {
                OreDredging.LOGGER.warn("Convergence recipe {} not found", id);
            }
        }

        // 缓存配方信息
        cachedRecipeInfos = infos;
        return infos;
    }

    private List<ResourceLocation> getRecipes() {
        return new ArrayList<>(recipes);
    }

    private ConvergenceRecipesData(List<ResourceLocation> recipes) {
        this(new HashSet<>(recipes));
        cachedRecipeInfos.clear();
    }

    /**
     * 预解析的配方信息，用于快速匹配和批量合成
     */
    public static class RecipeInfo {
        public final Map<MinerBundleItem.ItemKey, Integer> ingredients;  // 原料及所需数量
        public final ItemStack result;                   // 产物（原型）
        public final int resultCount;                    // 产物数量
        public final int totalIngredientCount;           // 原料总个数（用于容量计算）

        private RecipeInfo(Map<MinerBundleItem.ItemKey, Integer> ingredients, ItemStack result) {
            this.ingredients = ingredients;
            this.result = result;
            this.resultCount = result.getCount();
            this.totalIngredientCount = ingredients.values().stream().mapToInt(Integer::intValue).sum();
        }

        /**
         * 从 CraftingRecipe 构造 RecipeInfo，仅支持原料为具体物品的配方
         */
        public static RecipeInfo fromCraftingRecipe(CraftingRecipe recipe, RegistryAccess registryManager) {
            Map<MinerBundleItem.ItemKey, Integer> ingredients = new HashMap<>();
            for (Ingredient ingredient : recipe.getIngredients()) {
                ItemStack[] matchingStacks = ingredient.getItems();
                if (matchingStacks.length == 0) {
                    // 空原料
                    return null;
                }
                // 取第一个匹配物品作为代表，要求配方中每个原料格都是具体物品
                ItemStack representative = matchingStacks[0];
                // 如果同一个物品出现在多个原料格，合并数量
                MinerBundleItem.ItemKey key = new MinerBundleItem.ItemKey(representative);
                ingredients.merge(key, 1, Integer::sum);
            }
            // 产物
            ItemStack result = recipe.getResultItem(registryManager);
            if (result.isEmpty()) return null;
            return new RecipeInfo(ingredients, result);
        }
    }
}