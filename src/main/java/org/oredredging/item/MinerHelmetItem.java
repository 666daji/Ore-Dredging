package org.oredredging.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.oredredging.client.render.model.MinerHelmetItemRenderer;
import org.oredredging.client.render.model.MinerHelmetModel;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MinerHelmetItem extends ArmorItem {
    final MinerHelmetItemRenderer itemRenderer = new MinerHelmetItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    public MinerHelmetItem(ArmorMaterial material, Properties properties) {
        super(material, Type.HELMET, properties);
    }

    public enum ArmorMaterials implements ArmorMaterial {
        MINER_HELMET("miner_helmet", 200, new int[]{3, 0, 0, 0},  // helmet only
                10, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0F, 0.0F,
                () -> Ingredient.of(Items.IRON_INGOT));

        private final String name;
        private final int durabilityMultiplier;
        private final int[] protectionAmounts;
        private final int enchantability;
        private final SoundEvent equipSound;
        private final float toughness;
        private final float knockbackResistance;
        private final Lazy<Ingredient> repairIngredient;

        ArmorMaterials(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantability,
                       SoundEvent equipSound, float toughness, float knockbackResistance,
                       Supplier<Ingredient> repairIngredient) {
            this.name = name;
            this.durabilityMultiplier = durabilityMultiplier;
            this.protectionAmounts = protectionAmounts;
            this.enchantability = enchantability;
            this.equipSound = equipSound;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
            this.repairIngredient = Lazy.of(repairIngredient);
        }

        @Override
        public int getDurabilityForType(Type type) {
            return this.durabilityMultiplier;
        }

        @Override
        public int getDefenseForType(Type type) {
            // protectionAmounts 数组顺序: BOOTS, LEGGINGS, CHESTPLATE, HELMET
            return this.protectionAmounts[type.ordinal()];
        }

        @Override
        public int getEnchantmentValue() {
            return this.enchantability;
        }

        @Override
        public SoundEvent getEquipSound() {
            return this.equipSound;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return this.repairIngredient.get();
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public float getToughness() {
            return this.toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return this.knockbackResistance;
        }
    }

    // =================== Render ===================
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return itemRenderer;
            }

            @Override
            public @NotNull Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                       EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                // 只在头盔槽位生效
                if (equipmentSlot != EquipmentSlot.HEAD) {
                    return original;
                }

                MinerHelmetModel.setHeadAngles(original.head.xRot, original.head.yRot, original.head.zRot);
                return MinerHelmetModel.getCache();
            }
        });
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return MinerHelmetModel.TEXTURE.toString();
    }
}