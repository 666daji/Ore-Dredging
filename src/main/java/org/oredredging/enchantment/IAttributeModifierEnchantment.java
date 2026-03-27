package org.oredredging.enchantment;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

/**
 * 附魔需要修改物品的属性修饰符
 */
public interface IAttributeModifierEnchantment {
    /**
     * 为指定槽位的物品添加属性修饰符
     * @param stack 物品堆
     * @param slot 装备槽位
     * @param modifiers 待修改的属性修饰符集合（可变）
     */
    void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> modifiers);
}