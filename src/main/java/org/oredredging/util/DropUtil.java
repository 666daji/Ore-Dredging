package org.oredredging.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import org.oredredging.config.DropConfig;

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 管理所有定义的具有特殊掉落的方块。
 * 使用 DropConfigManager 加载配置，并提供破碎判断。
 */
public class DropUtil {
    // 当前生效的方块集合
    private static Set<Block> canCrushedBlocks;
    private static Set<Block> haveExtraDropBlocks;

    private static boolean mark;

    // 静态初始化：加载配置
    static {
        reload();
    }

    /**
     * 重新加载配置。
     */
    public static void reload() {
        var config = DropConfig.load();
        canCrushedBlocks = config.get("canCrushed");
        haveExtraDropBlocks = config.get("haveExtra");
    }

    /**
     * 保存当前配置到文件。
     */
    public static void saveConfig() {
        DropConfig.save(canCrushedBlocks, haveExtraDropBlocks);
    }

    /**
     * 获取当前可破碎方块集合（只读视图）。
     */
    public static Set<Block> getCanCrushedBlocks() {
        return Set.copyOf(canCrushedBlocks);
    }

    /**
     * 获取当前额外掉落方块集合（只读视图）。
     */
    public static Set<Block> getHaveExtraDropBlocks() {
        return Set.copyOf(haveExtraDropBlocks);
    }

    /**
     * 判断方块是否发生“破碎”并应使用 crushed 战利品表。
     */
    public static boolean isCrushed(BlockState state, LootContextParameterSet.Builder builder) {
        if (!canCrushedBlocks.contains(state.getBlock()) || isSilkTouch(builder)) {
            return false;
        }
        return RandomUtil.randomBoolean(0.25F);
    }

    /**
     * 判断方块是否发生“破碎+额外”并应附加 extra 战利品表。
     */
    public static boolean isCrushedAndExtra(BlockState state, LootContextParameterSet.Builder builder) {
        if (!haveExtraDropBlocks.contains(state.getBlock()) || isSilkTouch(builder)) {
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
            return false;
        } catch (NoSuchElementException exception) {
            return false;
        }
    }

    public static boolean isCanCrushedBlock(Block block) {
        return (canCrushedBlocks.contains(block) || haveExtraDropBlocks.contains(block)) && mark;
    }

    public static void clearMark() {
        mark = false;
    }

    public static void mark() {
        mark = true;
    }
}