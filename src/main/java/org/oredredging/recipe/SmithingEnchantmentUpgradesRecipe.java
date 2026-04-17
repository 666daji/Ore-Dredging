package org.oredredging.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.registry.ModRecipeSerializers;
import org.oredredging.util.RandomUtil;

import java.util.*;

public class SmithingEnchantmentUpgradesRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    protected final Ingredient template;
    protected final Ingredient addition;
    private final float probability;

    public SmithingEnchantmentUpgradesRecipe(ResourceLocation id, Ingredient template, Ingredient addition, float probability) {
        this.id = id;
        this.template = template;
        this.addition = addition;
        this.probability = probability;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return stack.isEnchanted();
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (!this.template.test(container.getItem(0)) ||
                !isBaseIngredient(container.getItem(1)) ||
                !this.addition.test(container.getItem(2))) {
            return false;
        }
        ItemStack base = container.getItem(1);
        return hasUngradedEnchantments(base);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack base = container.getItem(1);
        if (base.isEmpty()) return ItemStack.EMPTY;

        if (!hasUngradedEnchantments(base)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = base.copy();
        Map<Enchantment, Integer> enchantments =
                EnchantmentHelper.getEnchantments(result);
        if (enchantments.isEmpty()) return result;

        CompoundTag tag = result.getOrCreateTag();
        ListTag enhancedList = tag.getList("EnhancedEnchantments", Tag.TAG_STRING);
        Set<String> enhanced = new HashSet<>();
        for (int i = 0; i < enhancedList.size(); i++) {
            enhanced.add(enhancedList.getString(i));
        }

        Map<Enchantment, Integer> newEnchantments = new HashMap<>(enchantments);
        List<String> newEnhanced = new ArrayList<>(enhanced);
        boolean changed = false;

        for (Map.Entry<Enchantment, Integer> entry : newEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            ResourceLocation enchantmentId = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            if (enchantmentId == null) continue;

            String idStr = enchantmentId.toString();
            if (!enhanced.contains(idStr)) {
                if (RandomUtil.randomBoolean(probability)) {
                    newEnchantments.put(enchantment, entry.getValue() + 1);
                    newEnhanced.add(idStr);
                    changed = true;
                }
            }
        }

        if (changed) {
            // 清除原有附魔并设置新附魔
            EnchantmentHelper.setEnchantments(newEnchantments, result);
            ListTag newList = new ListTag();
            for (String s : newEnhanced) {
                newList.add(StringTag.valueOf(s));
            }
            result.getOrCreateTag().put("EnhancedEnchantments", newList);
        }

        result.getOrCreateTag().putBoolean("preview", true);
        return result;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SMITHING_ENCHANTMENT_UPGRADES.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 1;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    /**
     * 检查物品上是否存在未强化过的附魔
     */
    public static boolean hasUngradedEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments =
                EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) return false;

        CompoundTag tag = stack.getTag();
        if (tag == null) return true;

        ListTag enhancedList = tag.getList("EnhancedEnchantments", Tag.TAG_STRING);
        Set<String> enhanced = new HashSet<>();
        for (int i = 0; i < enhancedList.size(); i++) {
            enhanced.add(enhancedList.getString(i));
        }

        for (Enchantment enchantment : enchantments.keySet()) {
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            if (id != null && !enhanced.contains(id.toString())) {
                return true;
            }
        }
        return false;
    }

    public static class Serializer implements RecipeSerializer<SmithingEnchantmentUpgradesRecipe> {

        @Override
        public SmithingEnchantmentUpgradesRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "template"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "addition"));
            float probability = GsonHelper.getAsFloat(json, "probability");
            return new SmithingEnchantmentUpgradesRecipe(id, template, addition, probability);
        }

        @Override
        public SmithingEnchantmentUpgradesRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient template = Ingredient.fromNetwork(buf);
            Ingredient addition = Ingredient.fromNetwork(buf);
            float probability = buf.readFloat();
            return new SmithingEnchantmentUpgradesRecipe(id, template, addition, probability);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SmithingEnchantmentUpgradesRecipe recipe) {
            recipe.template.toNetwork(buf);
            recipe.addition.toNetwork(buf);
            buf.writeFloat(recipe.probability);
        }
    }
}