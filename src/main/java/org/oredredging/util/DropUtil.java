package org.oredredging.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import org.oredredging.config.CrushedDropsData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;

import java.util.List;
import java.util.NoSuchElementException;

public final class DropUtil {
    private static boolean mark;

    private DropUtil() {}

    /**
     * 获取当前可破碎方块列表。
     */
    private static List<Block> getCanCrushedBlocks() {
        CrushedDropsData data = ConfigManager.get(ModConfigs.CRUSHED_DROPS);
        return data != null ? data.canCrushed() : ModConfigs.CRUSHED_DROPS.defaultValue().canCrushed();
    }

    /**
     * 获取当前额外掉落方块列表。
     */
    private static List<Block> getHaveExtraDropBlocks() {
        CrushedDropsData data = ConfigManager.get(ModConfigs.CRUSHED_DROPS);
        return data != null ? data.haveExtraDrop() : ModConfigs.CRUSHED_DROPS.defaultValue().haveExtraDrop();
    }

    /**
     * 判断方块是否发生“破碎”并应使用 crushed 战利品表。
     */
    public static boolean isCrushed(BlockState state, LootContextParameterSet.Builder builder) {
        if (!getCanCrushedBlocks().contains(state.getBlock()) || isSilkTouch(builder)) {
            return false;
        }
        return RandomUtil.randomBoolean(0.25F);
    }

    /**
     * 判断方块是否发生“破碎+额外”并应附加 extra 战利品表。
     */
    public static boolean isCrushedAndExtra(BlockState state, LootContextParameterSet.Builder builder) {
        if (!getHaveExtraDropBlocks().contains(state.getBlock()) || isSilkTouch(builder)) {
            return false;
        }
        return RandomUtil.randomBoolean(0.25F);
    }

    /**
     * 检查物品是否具有精准采集附魔。
     */
    public static boolean isSilkTouch(LootContextParameterSet.Builder builder) {
        try {
            ItemStack tool = builder.get(LootContextParameters.TOOL);
            if (tool != null && !tool.isEmpty()) {
                return EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) != 0;
            }
        } catch (NoSuchElementException ignored) {
            // 上下文中没有工具，视为无精准采集
        }
        return false;
    }

    public static boolean isCanCrushedBlock(Block block) {
        return (getCanCrushedBlocks().contains(block) || getHaveExtraDropBlocks().contains(block)) && mark;
    }

    public static void clearMark() {
        mark = false;
    }

    public static void mark() {
        mark = true;
    }
}