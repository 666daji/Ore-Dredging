package org.oredredging.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.registry.ModRecipeSerializers;

import java.util.Objects;
import java.util.stream.Stream;

public class SmithingEnchantmentRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    protected final Ingredient template;
    protected final Ingredient addition;
    protected final EnchantmentInstance enchantmentEntry;

    public SmithingEnchantmentRecipe(ResourceLocation id, Ingredient template, Ingredient addition,
                                     EnchantmentInstance enchantmentEntry) {
        this.id = id;
        this.template = template;
        this.addition = addition;
        this.enchantmentEntry = enchantmentEntry;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return this.template.test(container.getItem(0)) &&
                isBaseIngredient(container.getItem(1)) &&
                this.addition.test(container.getItem(2));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack base = container.getItem(1).copy();
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantmentEntry.enchantment, base) == 0) {
            base.enchant(enchantmentEntry.enchantment, enchantmentEntry.level);
            return base;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return enchantmentEntry.enchantment.canEnchant(stack) &&
                net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantmentEntry.enchantment, stack) == 0;
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SMITHING_ENCHANTMENT.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 1;
    }

    // 可选：覆写 isSpecial 等方法，按需返回
    @Override
    public boolean isSpecial() {
        return true; // 避免在配方书中显示为普通配方
    }

    public static class Serializer implements RecipeSerializer<SmithingEnchantmentRecipe> {

        @Override
        public SmithingEnchantmentRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "template"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "addition"));
            EnchantmentInstance enchantmentEntry = enchantmentFromJson(json, "enchantment");
            return new SmithingEnchantmentRecipe(id, template, addition, enchantmentEntry);
        }

        @Override
        public SmithingEnchantmentRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient template = Ingredient.fromNetwork(buf);
            Ingredient addition = Ingredient.fromNetwork(buf);
            ResourceLocation enchantmentId = buf.readResourceLocation();
            int level = buf.readInt();
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(enchantmentId);
            if (enchantment == null) {
                throw new IllegalArgumentException("Unknown enchantment: " + enchantmentId);
            }
            EnchantmentInstance entry = new EnchantmentInstance(enchantment, level);
            return new SmithingEnchantmentRecipe(id, template, addition, entry);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SmithingEnchantmentRecipe recipe) {
            recipe.template.toNetwork(buf);
            recipe.addition.toNetwork(buf);
            ResourceLocation enchantmentId = ForgeRegistries.ENCHANTMENTS.getKey(recipe.enchantmentEntry.enchantment);
            if (enchantmentId == null) {
                throw new IllegalStateException("Unregistered enchantment: " + recipe.enchantmentEntry.enchantment);
            }
            buf.writeResourceLocation(enchantmentId);
            buf.writeInt(recipe.enchantmentEntry.level);
        }

        private EnchantmentInstance enchantmentFromJson(JsonObject json, String name) {
            if (json.has(name)) {
                JsonObject obj = GsonHelper.getAsJsonObject(json, name);
                ResourceLocation enchantmentId = ResourceLocation.tryParse(GsonHelper.getAsString(obj, "enchantment"));
                Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(enchantmentId);
                if (enchantment == null) {
                    throw new JsonSyntaxException("Unknown enchantment: " + enchantmentId);
                }
                int level = GsonHelper.getAsInt(obj, "level");
                return new EnchantmentInstance(enchantment, level);
            } else {
                throw new JsonSyntaxException("Missing " + name);
            }
        }
    }
}