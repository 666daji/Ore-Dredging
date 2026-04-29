package org.oredredging.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.oredredging.block.entity.ThunderSmelterPipeBlockEntity;
import org.oredredging.registry.ModRecipeSerializers;
import org.oredredging.registry.ModRecipes;

import java.util.Random;

public class ThunderSmeltRecipe implements Recipe<ThunderSmelterPipeBlockEntity> {
    private final Identifier id;
    private final Ingredient input;
    private final ItemStack output;
    private final @Nullable Item extraItem;
    private final int extraMin;
    private final int extraMax;

    public ThunderSmeltRecipe(Identifier id, Ingredient input, ItemStack output,
                              @Nullable Item extraItem, int extraMin, int extraMax) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.extraItem = extraItem;
        this.extraMin = extraMin;
        this.extraMax = extraMax;
    }

    @Override
    public boolean matches(ThunderSmelterPipeBlockEntity inventory, World world) {
        return input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(ThunderSmelterPipeBlockEntity inventory, DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.THUNDER_SMELT;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.THUNDER_SMELT;
    }

    public Ingredient getInput() {
        return input;
    }

    /** 额外产物物品
     * @return 可能为 null（表示无额外产物） */
    public @Nullable Item getExtraItem() {
        return extraItem;
    }

    public int getExtraMin() {
        return extraMin;
    }

    public int getExtraMax() {
        return extraMax;
    }

    /**
     * 根据给定的随机源生成本次熔炼的额外产物堆叠。
     * 若无额外产物，返回 {@link ItemStack#EMPTY}。
     */
    public ItemStack getExtraOutput(Random random) {
        if (extraItem == null) {
            return ItemStack.EMPTY;
        }
        int count = extraMin + random.nextInt(extraMax - extraMin + 1);
        return new ItemStack(extraItem, Math.max(0, count));
    }
}