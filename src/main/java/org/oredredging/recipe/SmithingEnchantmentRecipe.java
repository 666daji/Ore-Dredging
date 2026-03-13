package org.oredredging.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import org.oredredging.registry.ModRecipeSerializers;

import java.util.Objects;
import java.util.stream.Stream;

public class SmithingEnchantmentRecipe implements SmithingRecipe {
    private final Identifier id;
    protected final Ingredient template;
    protected final Ingredient addition;
    protected final EnchantmentLevelEntry enchantmentEntry;

    public SmithingEnchantmentRecipe(Identifier id, Ingredient template, Ingredient addition, EnchantmentLevelEntry enchantmentEntry) {
        this.id = id;
        this.template = template;
        this.addition = addition;
        this.enchantmentEntry = enchantmentEntry;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.template.test(inventory.getStack(0)) && testBase(inventory.getStack(1)) && this.addition.test(inventory.getStack(2));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack itemStack = inventory.getStack(1).copy();
        itemStack.addEnchantment(enchantmentEntry.enchantment, enchantmentEntry.level);

        return itemStack;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return enchantmentEntry.enchantment.isAcceptableItem(stack);
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SMITHING_ENCHANTMENT;
    }

    @Override
    public boolean isEmpty() {
        return Stream.of(this.template, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer implements RecipeSerializer<SmithingEnchantmentRecipe> {

        @Override
        public SmithingEnchantmentRecipe read(Identifier identifier, JsonObject jsonObject) {
            Ingredient template = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "template"));
            Ingredient addition = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "addition"));
            EnchantmentLevelEntry enchantmentLevelEntry = fromJson(jsonObject, "enchantment");
            return new SmithingEnchantmentRecipe(identifier, template, addition, enchantmentLevelEntry);
        }

        @Override
        public SmithingEnchantmentRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            Ingredient template = Ingredient.fromPacket(packetByteBuf);
            Ingredient addition = Ingredient.fromPacket(packetByteBuf);
            Identifier enchantmentId = packetByteBuf.readIdentifier();
            int level = packetByteBuf.readInt();

            Enchantment enchantment = Registries.ENCHANTMENT.get(enchantmentId);
            if (enchantment == null) {
                throw new IllegalArgumentException("Unknown enchantment: " + enchantmentId);
            }
            EnchantmentLevelEntry enchantmentEntry = new EnchantmentLevelEntry(enchantment, level);
            return new SmithingEnchantmentRecipe(identifier, template, addition, enchantmentEntry);
        }

        @Override
        public void write(PacketByteBuf packetByteBuf, SmithingEnchantmentRecipe recipe) {
            recipe.template.write(packetByteBuf);
            recipe.addition.write(packetByteBuf);
            Identifier enchantmentId = Registries.ENCHANTMENT.getId(recipe.enchantmentEntry.enchantment);
            if (enchantmentId == null) {
                throw new IllegalStateException("Unregistered enchantment: " + recipe.enchantmentEntry.enchantment);
            }
            packetByteBuf.writeIdentifier(enchantmentId);
            packetByteBuf.writeInt(recipe.enchantmentEntry.level);
        }

        private EnchantmentLevelEntry fromJson(JsonObject object, String name) {
            if (object.has(name)) {
                JsonObject object1 = JsonHelper.getObject(object, name);
                Identifier enchantmentId = Identifier.tryParse(JsonHelper.getString(object1, "enchantment"));
                Enchantment enchantment = Objects.requireNonNull(Registries.ENCHANTMENT.get(enchantmentId));
                int level = JsonHelper.getInt(object1, "level");

                return new EnchantmentLevelEntry(enchantment, level);
            } else {
                throw new JsonSyntaxException("Missing " + name);
            }
        }
    }
}
