package org.oredredging.item;

import java.util.function.Supplier;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public enum ModToolMaterials implements ToolMaterial {
    GEOLOGICAL_HAMMER(MiningLevels.DIAMOND, 1561, 7.0F, 4.0F, 10, () -> Ingredient.ofItems(Items.IRON_INGOT)),
    COLLAPSE_STONE_HAMMER(MiningLevels.DIAMOND, 1561, 1.0F, 2.0F, 10, () -> Ingredient.ofItems(Items.IRON_INGOT));

    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchant_ability;
    private final Supplier<Ingredient> repairIngredient;

    ModToolMaterials(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchant_ability, Supplier<Ingredient> repairIngredient) {
        this.miningLevel = miningLevel;
        this.itemDurability = itemDurability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchant_ability = enchant_ability;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurability() {
        return this.itemDurability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return this.miningLevel;
    }

    @Override
    public int getEnchantability() {
        return this.enchant_ability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
