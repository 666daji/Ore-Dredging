package org.oredredging.enchantment;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import org.oredredging.config.ConvergenceRecipesData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;
import org.oredredging.item.MinerBundleItem;
import org.oredredging.registry.ModEnchantments;

import java.util.*;

public class MinerBundleEnchantment extends Enchantment {
    public MinerBundleEnchantment(Rarity weight) {
        super(weight, EnchantmentCategory.WEAPON, EquipmentSlot.values());
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof MinerBundleItem;
    }

    /**
     * 触发聚拢效果。
     * @param bag    袋子物品栈
     * @param player 玩家
     */
    public static void triggerConverge(ItemStack bag, Player player) {
        if (!(bag.getItem() instanceof MinerBundleItem)) return;
        if (bag.getEnchantmentLevel(ModEnchantments.CONVERGENCE.get()) == 0) return;

        Level world = player != null ? player.level() : null;
        if (world == null) return;

        // 获取配方信息（缓存）
        ConvergenceRecipesData recipesData = ConfigManager.get(ModConfigs.CONVERGENCE_RECIPES);
        if (recipesData == null) return;

        List<ConvergenceRecipesData.RecipeInfo> recipeInfos = recipesData.getRecipeInfos(world);
        if (recipeInfos.isEmpty()) return;

        // 将袋子当前物品列表转换为计数映射 (ItemKey -> 数量)
        Map<MinerBundleItem.ItemKey, Integer> content = MinerBundleItem.buildContentMap(bag);
        int storage = MinerBundleItem.getStorage(bag);

        boolean changed = false;
        int loopGuard = 10; // 防止无限循环
        do {
            boolean anyCrafted = false;
            for (ConvergenceRecipesData.RecipeInfo info : recipeInfos) {
                // 计算当前配方最大可合成次数（考虑原料数量）
                int maxTimes = Integer.MAX_VALUE;
                for (Map.Entry<MinerBundleItem.ItemKey, Integer> need : info.ingredients.entrySet()) {
                    int have = content.getOrDefault(need.getKey(), 0);
                    maxTimes = Math.min(maxTimes, have / need.getValue());
                }
                if (maxTimes == 0) continue;

                // 检查容量限制
                int currentTotal = content.values().stream().mapToInt(Integer::intValue).sum();
                int resultTotalIncrease = info.resultCount - info.totalIngredientCount;
                long newTotal = currentTotal + (long) resultTotalIncrease * maxTimes;
                if (newTotal > storage) {
                    // 容量不足，减少次数
                    maxTimes = Math.min(maxTimes, (storage - currentTotal) / resultTotalIncrease);
                    if (maxTimes <= 0) continue;
                }

                // 执行合成
                for (Map.Entry<MinerBundleItem.ItemKey, Integer> need : info.ingredients.entrySet()) {
                    int newAmount = content.get(need.getKey()) - need.getValue() * maxTimes;
                    if (newAmount == 0) {
                        content.remove(need.getKey());
                    } else {
                        content.put(need.getKey(), newAmount);
                    }
                }
                // 添加产物
                MinerBundleItem.ItemKey resultKey = new MinerBundleItem.ItemKey(info.result);
                content.merge(resultKey, info.resultCount * maxTimes, Integer::sum);

                anyCrafted = true;
                changed = true;
            }
            if (!anyCrafted) break;
        } while (--loopGuard > 0);

        if (changed) {
            // 将修改后的计数映射写回袋子
            MinerBundleItem.writeContentToBag(bag, content);
            // 播放音效
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F,
                    0.8F + player.level().getRandom().nextFloat() * 0.4F);
        }
    }

    /**
     * 尝试将物品自动收纳到袋子中。
     * 会尽量多地收纳，并更新 toAdd 的数量。
     *
     * @param bundle    袋子物品栈（会被修改）
     * @param toAdd     要收纳的物品（会被修改，数量减少实际收纳的数量）
     * @param player    相关玩家，用于播放音效
     * @return true 表示至少收纳了一个物品
     */
    public static boolean tryAutoPickup(ItemStack bundle, ItemStack toAdd, Player player) {
        if (!(bundle.getItem() instanceof MinerBundleItem bundleItem)) return false;
        if (!MinerBundleItem.hasAutoPicking(bundle)) return false;
        if (!bundleItem.getAllowedItems().test(toAdd)) return false;

        int added = bundleItem.addStack(bundle, toAdd, player);
        if (added > 0) {
            // 减少待收纳物品的数量
            toAdd.shrink(added);
            bundleItem.playInsertSound(player);
            return true;
        }
        return false;
    }
}