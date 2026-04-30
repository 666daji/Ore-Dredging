package org.oredredging.enchantment;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.UUID;

public class StarPatternEnchantment extends Enchantment implements IAttributeModifierEnchantment {
    private static final EnumMap<ArmorItem.Type, UUID> ARMOR_BOOST_UUIDS = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, UUID.fromString("e5f6a7b8-c9d0-4123-9456-789abcdef123"));
        map.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("f6a7b8c9-d0e1-4234-9567-89abcdef2345"));
        map.put(ArmorItem.Type.LEGGINGS, UUID.fromString("a7b8c9d0-e1f2-4345-9678-9abcdef34567"));
        map.put(ArmorItem.Type.BOOTS, UUID.fromString("b8c9d0e1-f2a3-4456-9789-abcdef456789"));
    });

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("c9d0e1f2-a3b4-4567-989a-bcdef56789ab");

    public StarPatternEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot... slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem || stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return false;
    }

    @Override
    public void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> modifiers) {
        int level = EnchantmentHelper.getLevel(this, stack);
        if (level <= 0) {
            return;
        }

        // 处理盔甲：增加护甲值（GENERIC_ARMOR）
        if (stack.getItem() instanceof ArmorItem armorItem) {
            // 仅当槽位与盔甲部位匹配时生效
            if (armorItem.getSlotType() != slot) {
                return;
            }

            UUID uuid = ARMOR_BOOST_UUIDS.get(armorItem.getType());
            if (uuid == null) {
                return;
            }

            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    uuid,
                    "Star Pattern armor boost",
                    level * 4.0,          // 每级增加 4 点护甲值
                    EntityAttributeModifier.Operation.ADDITION
            );
            modifiers.put(EntityAttributes.GENERIC_ARMOR, modifier);
        }
        // 处理剑：增加攻击伤害（GENERIC_ATTACK_DAMAGE）
        else if (stack.getItem() instanceof SwordItem) {
            // 只在主手槽位添加修饰符（攻击伤害通常仅对主手生效）
            if (slot != EquipmentSlot.MAINHAND) {
                return;
            }

            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    ATTACK_DAMAGE_UUID,
                    "Star Pattern damage boost",
                    level * 2.0,          // 每级增加 2 点攻击伤害
                    EntityAttributeModifier.Operation.ADDITION
            );
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, modifier);
        }
    }
}