package org.oredredging.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.oredredging.config.CrushedDropsData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 破碎机制核心工具类。
 * <p>
 * 破碎行为定义：当玩家挖掘特定方块时，有概率触发“破碎”，从而改变掉落物。
 * 破碎分为两种类型（由 {@link CrushType} 定义）：
 * <ul>
 *     <li><b>替换破碎 (CRUSHED)</b>：将原战利品表替换为 "crushed/&lt;方块ID&gt;" 的战利品表。</li>
 *     <li><b>附加破碎 (EXTRA)</b>：在原战利品表基础上额外附加 "extra/&lt;方块ID&gt;" 战利品表。</li>
 * </ul>
 * 触发条件：
 * <ol>
 *     <li>方块属于对应破碎类型的配置列表。</li>
 *     <li>使用的工具不带有精准采集附魔。</li>
 *     <li>随机概率 25%（由 {@link RandomUtil#randomBoolean(float)} 决定）。</li>
 * </ol>
 * 若任意破碎类型触发，则会记录一个内部标记（{@code mark}），并在挖掘完成后播放大量破碎粒子效果。
 * 粒子效果仅在本次挖掘至少触发一次破碎时生效，且播放后自动清除标记。
 * <p>
 */
public final class DropUtil {
    private static boolean mark = false;

    private DropUtil() {}

    /**
     * 判断指定类型的破碎行为是否应该触发。
     *
     * @param state   方块状态
     * @param builder 战利品上下文构建器
     * @param type    破碎类型
     * @return 是否触发
     */
    public static boolean shouldTrigger(BlockState state, LootContextParameterSet.Builder builder, CrushType type) {
        if (!type.getBlockList().contains(state.getBlock()) || isSilkTouch(builder)) {
            return false;
        }

        boolean trigger = RandomUtil.randomBoolean(0.25F);

        if (trigger) {
            mark = true;
        }

        return trigger;
    }

    /**
     * 应用破碎粒子效果（仅当本次挖掘触发了任意破碎行为时生效）。
     *
     * @param state 方块状态
     * @param world 服务端世界
     * @param pos   方块位置
     */
    public static void applyCrushedEffect(BlockState state, ServerWorld world, BlockPos pos) {
        if (isAnyCrushTriggeredForBlock(state.getBlock())) {
            for (int i = 0; i < 30; i++) {
                world.spawnParticles(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                        pos.getX(), pos.getY(), pos.getZ(),
                        10, 0, 0, 0, 3.2
                );
            }
            clearMark();
        }
    }

    /**
     * 检查工具是否带有精准采集。
     */
    private static boolean isSilkTouch(LootContextParameterSet.Builder builder) {
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

    /**
     * 判断某个方块是否属于任意破碎类型且本次挖掘触发了破碎。
     */
    private static boolean isAnyCrushTriggeredForBlock(Block block) {
        if (!mark) return false;
        for (CrushType type : CrushType.values()) {
            if (type.getBlockList().contains(block)) {
                return true;
            }
        }
        return false;
    }

    private static void clearMark() {
        mark = false;
    }

    /**
     * 表示破碎的类型。
     */
    public enum CrushType {
        CRUSHED {
            @Override
            public List<Block> getBlockList() {
                CrushedDropsData data = ConfigManager.get(ModConfigs.CRUSHED_DROPS);
                return data != null ? data.canCrushed() : ModConfigs.CRUSHED_DROPS.defaultValue().canCrushed();
            }
        },
        EXTRA {
            @Override
            public List<Block> getBlockList() {
                CrushedDropsData data = ConfigManager.get(ModConfigs.CRUSHED_DROPS);
                return data != null ? data.haveExtraDrop() : ModConfigs.CRUSHED_DROPS.defaultValue().haveExtraDrop();
            }
        };

        public abstract List<Block> getBlockList();
    }
}