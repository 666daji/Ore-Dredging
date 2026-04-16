package org.oredredging.item;

import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class GeologicalHammerItem extends PickaxeItem implements CrushedDropGain{
    public GeologicalHammerItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getProbability(int original) {
        return (int) (original * 0.31F);
    }
}
