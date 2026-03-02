package org.oredredging.registry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.enchantment.MinerBundleEnchantment;

public class ModEnchantments {
    // 洞天
    public static final Enchantment EXPANSION = register("expansion", new MinerBundleEnchantment(Enchantment.Rarity.UNCOMMON));

    // 聚拢
    public static final Enchantment CONVERGENCE = register("convergence", new MinerBundleEnchantment(Enchantment.Rarity.UNCOMMON));

    // 收纳
    public static final Enchantment AUTO_PICKING = register("auto_picking", new MinerBundleEnchantment(Enchantment.Rarity.UNCOMMON));

    private static Enchantment register(String id, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(OreDredging.MOD_ID, id), enchantment);
    }

    public static void registerAll() {}
}
