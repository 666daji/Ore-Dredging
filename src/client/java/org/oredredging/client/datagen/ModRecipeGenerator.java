package org.oredredging.client.datagen;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.registry.ModBlocks;
import org.oredredging.registry.ModItems;

import java.util.function.Consumer;

public class ModRecipeGenerator extends RecipeProvider {
    public ModRecipeGenerator(DataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        // 遍历所有染料颜色
        for (DyeColor color : DyeColor.values()) {
            String colorName = color.getName();
            ItemConvertible dye = DyeItem.byColor(color);
            ItemConvertible concrete = Registries.ITEM.get(new Identifier( colorName + "_concrete_powder"));

            if (concrete != Items.AIR)  {
                ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, concrete, 8)
                        .input(dye)
                        .input(ModItems.GRAY_QUARTZ)
                        .input(ModItems.GRAY_QUARTZ)
                        .input(Items.SAND)
                        .input(Items.SAND)
                        .input(Items.GRAVEL)
                        .input(Items.GRAVEL)
                        .criterion("has_gravel", conditionsFromItem(Items.GRAVEL))
                        .criterion("has_dye", conditionsFromItem(dye))
                        .offerTo(exporter, new Identifier(OreDredging.MOD_ID, colorName + "_concrete_from_quartz"));
            }
        }

        // 粗金属块半砖
        offerSlabRecipe(exporter, ModBlocks.RAW_IRON_SLAB, Blocks.RAW_IRON_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.RAW_COPPER_SLAB, Blocks.RAW_COPPER_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.RAW_GOLD_SLAB, Blocks.RAW_GOLD_BLOCK);

        // 金属块半砖
        offerSlabRecipe(exporter, ModBlocks.IRON_SLAB, Blocks.IRON_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.GOLD_SLAB, Blocks.GOLD_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.COPPER_SLAB, Blocks.COPPER_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.NETHERITE_SLAB, Blocks.NETHERITE_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.DIAMOND_SLAB, Blocks.DIAMOND_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.EMERALD_SLAB, Blocks.EMERALD_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.LAPIS_SLAB, Blocks.LAPIS_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.REDSTONE_SLAB, Blocks.REDSTONE_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.COAL_SLAB, Blocks.COAL_BLOCK);
    }

    /**
     * 生成半砖合成配方：3个完整块 → 6个半砖
     * @param exporter 配方导出器
     * @param slab 半砖物品/方块
     * @param fullBlock 完整块
     */
    private void offerSlabRecipe(Consumer<RecipeJsonProvider> exporter, ItemConvertible slab, ItemConvertible fullBlock) {
        offerSlabRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, slab, fullBlock);

        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, fullBlock, 1)
                .pattern("#")
                .pattern("#")
                .input('#', slab)
                .criterion("has_" + getItemPath(slab), RecipeProvider.conditionsFromItem(slab))
                .offerTo(exporter, getSlabReversibleId(slab));
    }

    /**
     * 生成逆向分解配方的ID（在原ID后加 _reversible）
     */
    private Identifier getSlabReversibleId(ItemConvertible slab) {
        return Identifier.of(OreDredging.MOD_ID, getItemPath(slab) + "_reversible");
    }
}
