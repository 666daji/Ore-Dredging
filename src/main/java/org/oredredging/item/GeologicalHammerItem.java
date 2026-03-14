package org.oredredging.item;

import net.minecraft.item.PickaxeItem;

public class GeologicalHammerItem extends PickaxeItem implements CrushedDropGain{
    public GeologicalHammerItem(int attackDamage, float attackSpeed, Settings settings) {
        super(ModToolMaterials.COLLAPSE_STONE_HAMMER, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getProbability(int original) {
        return (int) (original * 1.25F);
    }
}
