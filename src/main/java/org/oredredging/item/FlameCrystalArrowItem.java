package org.oredredging.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.oredredging.entity.FlameCrystalArrowEntity;

public class FlameCrystalArrowItem extends ArrowItem {
    public FlameCrystalArrowItem(Settings settings) {
        super(settings);
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
        return new FlameCrystalArrowEntity(shooter, world);
    }
}
