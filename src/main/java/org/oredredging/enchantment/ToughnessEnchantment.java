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
import net.minecraft.util.Util;

import java.util.EnumMap;
import java.util.UUID;

public class ToughnessEnchantment extends Enchantment implements IAttributeModifierEnchantment {
    private static final EnumMap<ArmorItem.Type, UUID> TOUGHNESS_BOOST_UUIDS = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, UUID.fromString("a1b2c3d4-e5f6-4789-8123-456789abcdef"));
        map.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("b2c3d4e5-f6a7-4901-9234-56789abcdef0"));
        map.put(ArmorItem.Type.LEGGINGS, UUID.fromString("c3d4e5f6-a7b8-4012-9345-6789abcdef12"));
        map.put(ArmorItem.Type.BOOTS, UUID.fromString("d4e5f6a7-b8c9-4123-9456-789abcdef123"));
    });

    public ToughnessEnchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot... slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return false;
    }

    @Override
    public void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> modifiers) {
        // 仅对盔甲且槽位匹配时生效
        if (!(stack.getItem() instanceof ArmorItem armorItem) || armorItem.getSlotType() != slot) {
            return;
        }

        int level = EnchantmentHelper.getLevel(this, stack);
        if (level <= 0) {
            return;
        }

        UUID uuid = TOUGHNESS_BOOST_UUIDS.get(armorItem.getType());
        if (uuid == null) {
            return;
        }

        // 每级增加 2 点护甲韧性
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                uuid,
                "Toughness boost",
                level * 2.0,
                EntityAttributeModifier.Operation.ADDITION
        );
        modifiers.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, modifier);
    }
}