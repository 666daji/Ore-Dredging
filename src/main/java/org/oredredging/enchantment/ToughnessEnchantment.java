package org.oredredging.enchantment;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;

import java.util.EnumMap;
import java.util.UUID;

public class ToughnessEnchantment extends Enchantment implements IAttributeModifierEnchantment {
    private static final EnumMap<ArmorItem.Type, UUID> TOUGHNESS_BOOST_UUIDS = new EnumMap<>(ArmorItem.Type.class);

    static {
        TOUGHNESS_BOOST_UUIDS.put(ArmorItem.Type.HELMET, UUID.fromString("a1b2c3d4-e5f6-4789-8123-456789abcdef"));
        TOUGHNESS_BOOST_UUIDS.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("b2c3d4e5-f6a7-4901-9234-56789abcdef0"));
        TOUGHNESS_BOOST_UUIDS.put(ArmorItem.Type.LEGGINGS, UUID.fromString("c3d4e5f6-a7b8-4012-9345-6789abcdef12"));
        TOUGHNESS_BOOST_UUIDS.put(ArmorItem.Type.BOOTS, UUID.fromString("d4e5f6a7-b8c9-4123-9456-789abcdef123"));
    }

    public ToughnessEnchantment(Rarity rarity, EnchantmentCategory category, EquipmentSlot... slots) {
        super(rarity, category, slots);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, ItemAttributeModifierEvent event) {
        // 仅对盔甲且槽位匹配时生效
        if (!(stack.getItem() instanceof ArmorItem armorItem) || armorItem.getEquipmentSlot() != slot) {
            return;
        }

        int level = EnchantmentHelper.getTagEnchantmentLevel(this, stack);
        if (level <= 0) {
            return;
        }

        UUID uuid = TOUGHNESS_BOOST_UUIDS.get(armorItem.getType());
        if (uuid == null) {
            return;
        }

        // 每级增加 2 点护甲韧性
        AttributeModifier modifier = new AttributeModifier(
                uuid,
                "Toughness boost",
                level * 2.0,
                AttributeModifier.Operation.ADDITION
        );

        event.addModifier(Attributes.ARMOR_TOUGHNESS, modifier);
    }
}