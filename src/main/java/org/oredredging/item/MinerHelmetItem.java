package org.oredredging.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Supplier;

public class MinerHelmetItem extends ArmorItem {
    public MinerHelmetItem(ArmorMaterial material, Settings settings) {
        super(material, Type.HELMET, settings);
    }

    public enum ArmorMaterials implements StringIdentifiable, ArmorMaterial {
        MINER_HELMET("miner_helmet", 200, 39, 1,
                SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.ofItems(Items.IRON_INGOT));

        private final String name;
        private final int durability;
        private final int protectionAmounts;
        private final int enchant_ability;
        private final SoundEvent equipSound;
        private final float toughness;
        private final float knockbackResistance;
        private final Supplier<Ingredient> repairIngredientSupplier;

        ArmorMaterials(
                String name,
                int durability,
                int protectionAmounts,
                int enchant_ability,
                SoundEvent equipSound,
                float toughness,
                float knockbackResistance,
                Supplier<Ingredient> repairIngredientSupplier
        ) {
            this.name = name;
            this.durability = durability;
            this.protectionAmounts = protectionAmounts;
            this.enchant_ability = enchant_ability;
            this.equipSound = equipSound;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
            this.repairIngredientSupplier = repairIngredientSupplier;
        }

        @Override
        public int getDurability(ArmorItem.Type type) {
            return this.durability;
        }

        @Override
        public int getProtection(ArmorItem.Type type) {
            return this.protectionAmounts;
        }

        @Override
        public int getEnchantability() {
            return this.enchant_ability;
        }

        @Override
        public SoundEvent getEquipSound() {
            return this.equipSound;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return this.repairIngredientSupplier.get();
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public float getToughness() {
            return this.toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return this.knockbackResistance;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
