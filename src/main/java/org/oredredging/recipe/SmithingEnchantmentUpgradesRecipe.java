package org.oredredging.recipe;

import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import org.oredredging.registry.ModRecipeSerializers;
import org.oredredging.util.RandomUtil;

import java.util.*;

public class SmithingEnchantmentUpgradesRecipe implements SmithingRecipe {
    private final Identifier id;
    protected final Ingredient template;
    protected final Ingredient addition;
    private final float probability;

    public SmithingEnchantmentUpgradesRecipe(Identifier id, Ingredient template, Ingredient addition, float probability) {
        this.id = id;
        this.template = template;
        this.addition = addition;
        this.probability = probability;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return stack.hasEnchantments();
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.template.test(inventory.getStack(0)) &&
                testBase(inventory.getStack(1)) &&
                this.addition.test(inventory.getStack(2));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack base = inventory.getStack(1);
        if (base.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = base.copy();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(result);
        if (enchantments.isEmpty()) return result; // 没有附魔，直接返回

        // 获取已强化附魔记录
        NbtCompound tag = result.getOrCreateNbt();
        NbtList enhancedList = tag.getList("EnhancedEnchantments", NbtElement.STRING_TYPE);
        Set<String> enhanced = new HashSet<>();
        for (int i = 0; i < enhancedList.size(); i++) {
            enhanced.add(enhancedList.getString(i));
        }

        // 准备新的附魔映射和已强化列表
        Map<Enchantment, Integer> newEnchantments = new HashMap<>(enchantments);
        List<String> newEnhanced = new ArrayList<>(enhanced);
        boolean changed = false;

        Registry<Enchantment> enchantmentRegistry = registryManager.get(RegistryKeys.ENCHANTMENT);

        for (Map.Entry<Enchantment, Integer> entry : newEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            Identifier enchantmentId = enchantmentRegistry.getId(enchantment);
            if (enchantmentId == null) continue;

            String enchantmentIdStr = enchantmentId.toString();
            if (!enhanced.contains(enchantmentIdStr)) {
                if (RandomUtil.randomBoolean(probability)) {
                    newEnchantments.put(enchantment, level + 1);
                    newEnhanced.add(enchantmentIdStr);
                    changed = true;
                }
            }
        }

        if (changed) {
            // 应用新附魔
            EnchantmentHelper.set(newEnchantments, result);
            // 更新已强化列表
            NbtList newList = new NbtList();
            for (String s : newEnhanced) {
                newList.add(NbtString.of(s));
            }
            result.getOrCreateNbt().put("EnhancedEnchantments", newList);
        }

        // 添加预览标记
        result.getOrCreateNbt().putBoolean("preview", true);

        return result;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SMITHING_ENCHANTMENT_UPGRADES;
    }

    public static class Serializer implements RecipeSerializer<SmithingEnchantmentUpgradesRecipe> {

        @Override
        public SmithingEnchantmentUpgradesRecipe read(Identifier id, JsonObject json) {
            Ingredient template = Ingredient.fromJson(JsonHelper.getElement(json, "template"));
            Ingredient addition = Ingredient.fromJson(JsonHelper.getElement(json, "addition"));
            float probability = JsonHelper.getFloat(json, "probability");
            return new SmithingEnchantmentUpgradesRecipe(id, template, addition, probability);
        }

        @Override
        public SmithingEnchantmentUpgradesRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient template = Ingredient.fromPacket(buf);
            Ingredient addition = Ingredient.fromPacket(buf);
            float probability = buf.readFloat();
            return new SmithingEnchantmentUpgradesRecipe(id, template, addition, probability);
        }

        @Override
        public void write(PacketByteBuf buf, SmithingEnchantmentUpgradesRecipe recipe) {
            recipe.template.write(buf);
            recipe.addition.write(buf);
            buf.writeFloat(recipe.probability);
        }
    }
}