package org.oredredging.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CimeliaItem extends Item {
    public static final Style INTRODUCTION_STYLE;
    public static final String INTRODUCTION_PREFIX = "ore_dredging.cimelia_introduction.";

    protected final Category category;
    protected String introductionKey;
    protected final int lineCount;

    public CimeliaItem(Settings settings, Category category, int lineCount) {
        super(settings.rarity(Rarity.UNCOMMON));
        this.category = category;
        this.lineCount = lineCount;
    }

    /**
     * 获取显示名称后缀，包含类别样式。
     *
     * @return 带样式的类别文本
     */
    protected Text getCategoryDisplay() {
        return Text.translatable("ore_dredging.cimelia." + this.category.asString())
                .setStyle(category.getDisplayStyle());
    }

    /**
     * 获取指定行数的简介文本。
     *
     * @param line 行号（从1开始）
     * @return 带样式的简介文本
     */
    public Text getIntroduction(int line) {
        if (introductionKey == null) {
            // 懒加载简介的翻译键前缀，基于物品ID的路径
            String path = Registries.ITEM.getId(this).getPath();
            introductionKey = INTRODUCTION_PREFIX + path;
        }

        return Text.translatable(introductionKey + "_" + line).setStyle(INTRODUCTION_STYLE);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(getCategoryDisplay());
        for (int i = 1; i <= lineCount; i++) {
            tooltip.add(getIntroduction(i));
        }
    }

    /**
     * 表示珍宝的类别，包含其特有的样式。
     */
    public enum Category implements StringIdentifiable {
        NATURE("nature", Style.EMPTY.withColor(Formatting.GREEN).withItalic(true)),
        ANCIENT("ancient", Style.EMPTY.withColor(Formatting.GOLD).withItalic(true));

        private final String id;
        private final Style displayStyle;

        Category(String id, Style displayStyle) {
            this.id = id;
            this.displayStyle = displayStyle;
        }

        @Override
        public String asString() {
            return id;
        }

        public Style getDisplayStyle() {
            return displayStyle;
        }
    }

    static {
        INTRODUCTION_STYLE = Style.EMPTY.withColor(0x9A3C78).withItalic(true);
    }
}