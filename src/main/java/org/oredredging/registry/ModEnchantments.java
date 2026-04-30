package org.oredredging.registry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.enchantment.HeavyEnchantment;
import org.oredredging.enchantment.MinerBundleEnchantment;
import org.oredredging.enchantment.StarPatternEnchantment;
import org.oredredging.enchantment.ToughnessEnchantment;

public class ModEnchantments {
    // 洞天
    public static final Enchantment EXPANSION = register("expansion", new MinerBundleEnchantment(Enchantment.Rarity.RARE) {
        @Override
        public int getMaxLevel() {
            return 3;
        }

        @Override
        public int getMinPower(int level) {
            return super.getMinPower(level) - 7;
        }

        @Override
        public int getMaxPower(int level) {
            return super.getMaxPower(level) + 5;
        }
    });

    // 聚拢
    public static final Enchantment CONVERGENCE = register("convergence", new MinerBundleEnchantment(Enchantment.Rarity.VERY_RARE) {
        @Override
        public int getMinPower(int level) {
            return 25;
        }

        @Override
        public int getMaxPower(int level) {
            return 30;
        }
    });

    // 收纳
    public static final Enchantment AUTO_PICKING = register("auto_picking", new MinerBundleEnchantment(Enchantment.Rarity.VERY_RARE) {
        @Override
        public int getMinPower(int level) {
            return 10;
        }

        @Override
        public int getMaxPower(int level) {
            return 30;
        }
    });

    // 坚韧
    public static final Enchantment TOUGHNESS = register("toughness", new ToughnessEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.ARMOR, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    // 星纹
    public static final Enchantment STAR_PATTERN = register("star_pattern", new StarPatternEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentTarget.ARMOR, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    // 不屈意志
    public static final Enchantment UNYIELDING = register("unyielding", new Enchantment(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.ARMOR, EquipmentSlot.values()) {
        @Override
        public boolean isAvailableForRandomSelection() {
            return false;
        }

        @Override
        public int getMinPower(int level) {
            return super.getMinPower(level) - 7;
        }

        @Override
        public int getMaxPower(int level) {
            return super.getMaxPower(level) + 5;
        }
    });

    // 沉重
    public static final Enchantment HEAVY = register("heavy", new HeavyEnchantment(Enchantment.Rarity.RARE, EnchantmentTarget.WEAPON, EquipmentSlot.MAINHAND));

    private static Enchantment register(String id, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(OreDredging.MOD_ID, id), enchantment);
    }

    public static void registerAll() {}
}
