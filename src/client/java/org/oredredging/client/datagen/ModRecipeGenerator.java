package org.oredredging.client.datagen;

import net.minecraft.data.DataOutput;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
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
    }
}
