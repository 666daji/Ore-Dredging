package org.oredredging.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.oredredging.enchantment.IAttributeModifierEnchantment;
import org.oredredging.recipe.SmithingEnchantmentUpgradesRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean hasNbt();

    @Shadow
    public abstract NbtCompound getOrCreateNbt();

    @Inject(method = "getAttributeModifiers", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"), cancellable = true)
    private void applyEnchantmentAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack self = (ItemStack)(Object)this;

        // 获取原始修饰符
        Multimap<EntityAttribute, EntityAttributeModifier> original = getItem().getAttributeModifiers(slot);
        Multimap<EntityAttribute, EntityAttributeModifier> modified = HashMultimap.create(original);

        // 获取物品上的所有附魔
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(self);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment instanceof IAttributeModifierEnchantment modifierEnchantment) {
                modifierEnchantment.addAttributeModifiers(self, slot, modified);
            }
        }

        // 如果没有任何修改，直接返回原始值
        if (modified.equals(original)) {
            return;
        }

        cir.setReturnValue(modified);
    }


    @Inject(method = "onCraft", at = @At("HEAD"))
    private void onCraft(World world, PlayerEntity player, int amount, CallbackInfo ci) {
        if (hasNbt()) {
            NbtCompound nbt = getOrCreateNbt();
            nbt.remove("preview");
        }
    }

    @WrapOperation(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendEnchantments(Ljava/util/List;Lnet/minecraft/nbt/NbtList;)V")
    )
    private void wrapAppendEnchantments(List<Text> tooltip, NbtList enchantments, Operation<Void> original) {
        ItemStack self = (ItemStack)(Object)this;

        // 如果物品有 preview 标记，自定义显示附魔（只显示名称，不显示等级）
        if (self.hasNbt() && self.getNbt().contains("preview")) {
            for (int i = 0; i < enchantments.size(); i++) {
                NbtCompound nbt = enchantments.getCompound(i);
                Registries.ENCHANTMENT.getOrEmpty(EnchantmentHelper.getIdFromNbt(nbt)).ifPresent(enchantment -> {
                    Text name = Text.translatable(enchantment.getTranslationKey());
                    tooltip.add(name.copy().formatted(Formatting.GRAY));
                });
            }
        } else {
            original.call(tooltip, enchantments);

            // 充盈提示
            if (!SmithingEnchantmentUpgradesRecipe.hasUngradedEnchantments(self) && self.hasEnchantments()) {
                tooltip.add(Text.translatable("ore_dredging.tootip.energetic").setStyle(
                        Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withBold(true)
                ));
            }
        }
    }
}
