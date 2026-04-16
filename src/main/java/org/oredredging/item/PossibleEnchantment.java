package org.oredredging.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.oredredging.mixin.EnchantmentHelperMixin;

import java.util.List;

/**
 * 表示物品具有自定义的可随机附魔。
 * <p>由于原版的可随机附魔被{@linkplain EnchantmentCategory}限定了。
 * 所以出现了这个工具用于自定义允许的随机附魔。</p>
 * @see EnchantmentHelperMixin
 */
public interface PossibleEnchantment {
    /**
     * 获取新的随机附魔条目列表。
     *
     * @param original 原附魔条目列表
     * @param stack 原堆栈
     * @return 新的附魔条目列表
     */
    List<EnchantmentInstance> modifyList(List<EnchantmentInstance> original, int power, ItemStack stack, boolean treasureAllowed);

    /**
     * 根据附魔功率，返回该附魔可用的最高等级条目中的等级选择逻辑。
     *
     * @param enchantment 目标附魔
     * @param power       附魔台功率（即周围书架数量影响的数值）
     * @return 该附魔在给定功率下可用的最高等级条目，如果无可用等级则返回 {@code null}
     */
    static EnchantmentInstance getBestLevelEntry(Enchantment enchantment, int power) {
        for (int i = enchantment.getMaxLevel(); i >= enchantment.getMinLevel(); i--) {
            if (power >= enchantment.getMinCost(i) && power <= enchantment.getMaxCost(i)) {
                return new EnchantmentInstance(enchantment, i);
            }
        }
        return null;
    }
}
