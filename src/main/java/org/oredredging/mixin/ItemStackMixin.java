package org.oredredging.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.oredredging.enchantment.ToughnessEnchantment;
import org.oredredging.registry.ModEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getAttributeModifiers", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"), cancellable = true)
    private void addedToughness(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack self = (ItemStack)(Object)this;
        // 仅对盔甲生效，且必须是指定槽位（物品本应装备的槽位）
        if (!(self.getItem() instanceof ArmorItem armorItem) || armorItem.getSlotType() != slot) {
            return;
        }

        // 获取附魔等级
        int level = EnchantmentHelper.getLevel(ModEnchantments.TOUGHNESS, self);
        if (level <= 0) {
            return;
        }

        // 获取原始修饰符
        Multimap<EntityAttribute, EntityAttributeModifier> original = getItem().getAttributeModifiers(slot);
        // 创建可变的副本
        Multimap<EntityAttribute, EntityAttributeModifier> modified = HashMultimap.create(original);

        // 根据盔甲类型获取对应的 UUID
        ArmorItem.Type armorType = armorItem.getType();
        UUID uuid = ToughnessEnchantment.TOUGHNESS_BOOST_UUIDS.get(armorType);
        if (uuid == null) return;

        // 创建新的护甲韧性修饰符：每级 +2
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                uuid,
                "Toughness boost",
                level * 2.0,
                EntityAttributeModifier.Operation.ADDITION
        );
        modified.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, modifier);

        cir.setReturnValue(modified);
    }
}
