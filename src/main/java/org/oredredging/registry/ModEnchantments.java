package org.oredredging.registry;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.oredredging.OreDredging;
import org.oredredging.enchantment.HeavyEnchantment;
import org.oredredging.enchantment.MinerBundleEnchantment;
import org.oredredging.enchantment.ToughnessEnchantment;

import java.util.function.Supplier;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, OreDredging.MOD_ID);

    // 洞天
    public static final RegistryObject<Enchantment> EXPANSION = register("expansion",
            () -> new MinerBundleEnchantment(Enchantment.Rarity.RARE) {
                @Override
                public int getMaxLevel() { return 3; }
                @Override
                public int getMinCost(int level) { return super.getMinCost(level) - 7; }
                @Override
                public int getMaxCost(int level) { return super.getMaxCost(level) + 5; }
            });

    // 聚拢
    public static final RegistryObject<Enchantment> CONVERGENCE = register("convergence",
            () -> new MinerBundleEnchantment(Enchantment.Rarity.VERY_RARE) {
                @Override
                public int getMinCost(int level) { return 25; }
                @Override
                public int getMaxCost(int level) { return 30; }
            });

    // 收纳
    public static final RegistryObject<Enchantment> AUTO_PICKING = register("auto_picking",
            () -> new MinerBundleEnchantment(Enchantment.Rarity.VERY_RARE) {
                @Override
                public int getMinCost(int level) { return 10; }
                @Override
                public int getMaxCost(int level) { return 30; }
            });

    // 坚韧
    public static final RegistryObject<Enchantment> TOUGHNESS = register("toughness",
            () -> new ToughnessEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.ARMOR,
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    // 不屈意志
    public static final RegistryObject<Enchantment> UNYIELDING = register("unyielding",
            () -> new Enchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.ARMOR, EquipmentSlot.values()) {
                @Override
                public boolean isTreasureOnly() { return true; }
                @Override
                public int getMinCost(int level) { return super.getMinCost(level) - 7; }
                @Override
                public int getMaxCost(int level) { return super.getMaxCost(level) + 5; }
            });

    // 沉重
    public static final RegistryObject<Enchantment> HEAVY = register("heavy",
            () -> new HeavyEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON, EquipmentSlot.MAINHAND));

    private static RegistryObject<Enchantment> register(String id, Supplier<Enchantment> supplier) {
        return ENCHANTMENTS.register(id, supplier);
    }

    public static void registerAll(IEventBus modEventBus) {
        ENCHANTMENTS.register(modEventBus);
    }
}