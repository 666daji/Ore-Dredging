package org.oredredging.registry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.enchantment.MinerBundleEnchantment;
import org.oredredging.enchantment.ToughnessEnchantment;

public class ModEnchantments {
    // 洞天
    public static final Enchantment EXPANSION = register("expansion", new MinerBundleEnchantment(Enchantment.Rarity.RARE) {
        @Override
        public int getMaxLevel() {
            return 3;
        }
    });

    // 聚拢
    public static final Enchantment CONVERGENCE = register("convergence", new MinerBundleEnchantment(Enchantment.Rarity.VERY_RARE));

    // 收纳
    public static final Enchantment AUTO_PICKING = register("auto_picking", new MinerBundleEnchantment(Enchantment.Rarity.VERY_RARE));

    // 坚韧
    public static final Enchantment TOUGHNESS = register("toughness", new ToughnessEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.ARMOR, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    // 不屈意志
    public static final Enchantment UNYIELDING = register("unyielding", new Enchantment(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.ARMOR, EquipmentSlot.values()) {});

    private static Enchantment register(String id, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(OreDredging.MOD_ID, id), enchantment);
    }

    public static void registerAll() {}
}
