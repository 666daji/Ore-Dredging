package org.oredredging.item;

import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;

public class GeologicalHammerItem extends PickaxeItem implements CrushedDropGain{
    public GeologicalHammerItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getProbability(int original) {
        return (int) (original * 0.31F);
    }
}
