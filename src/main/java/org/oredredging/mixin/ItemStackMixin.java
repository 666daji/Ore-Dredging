package org.oredredging.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.recipe.SmithingEnchantmentUpgradesRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean hasTag();

    @Shadow
    public abstract CompoundTag getOrCreateTag();

    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void onCraft(Level world, Player player, int amount, CallbackInfo ci) {
        if (hasTag()) {
            CompoundTag nbt = getOrCreateTag();
            nbt.remove("preview");
        }
    }

    @WrapOperation(
            method = "getTooltipLines",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendEnchantmentNames(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V")
    )
    private void wrapAppendEnchantments(List<Component> tooltip, ListTag enchantments, Operation<Void> original) {
        ItemStack self = (ItemStack)(Object)this;

        // 如果物品有 preview 标记，自定义显示附魔（只显示名称，不显示等级）
        if (self.hasTag() && self.getTag().contains("preview")) {
            for (int i = 0; i < enchantments.size(); i++) {
                CompoundTag compoundtag = enchantments.getCompound(i);
                Optional<Enchantment> enchantment = Optional.ofNullable(ForgeRegistries.ENCHANTMENTS.getValue(EnchantmentHelper.getEnchantmentId(compoundtag)));
                enchantment.ifPresent((p_41708_) -> {
                    tooltip.add(p_41708_.getFullname(EnchantmentHelper.getEnchantmentLevel(compoundtag)));
                });
            }
        } else {
            original.call(tooltip, enchantments);

            // 充盈提示
            if (!SmithingEnchantmentUpgradesRecipe.hasUngradedEnchantments(self) && self.isEnchanted()) {
                tooltip.add(Component.translatable("ore_dredging.tootip.energetic").setStyle(
                        Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withBold(true)
                ));
            }
        }
    }
}