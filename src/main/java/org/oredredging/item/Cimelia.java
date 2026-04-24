package org.oredredging.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

import java.util.List;

public interface Cimelia {
    Style INTRODUCTION_STYLE = Style.EMPTY.withColor(0x9A3C78).withItalic(true);
    String INTRODUCTION_PREFIX = "ore_dredging.cimelia_introduction.";

    Category getCategory();

    int getLineCount();

    /**
     * 获取指定行数的简介文本。
     *
     * @param line 行号（从1开始）
     * @return 带样式的简介文本
     */
    default Text getIntroduction(int line, Item item) {
        String path = Registries.ITEM.getId(item).getPath();
        return Text.translatable(INTRODUCTION_PREFIX + path + "_" + line).setStyle(INTRODUCTION_STYLE);
    }

    /**
     * 获取显示名称后缀，包含类别样式。
     *
     * @return 带样式的类别文本
     */
    default Text getCategoryDisplay() {
        return Text.translatable("ore_dredging.cimelia." + this.getCategory().asString())
                .setStyle(getCategory().getDisplayStyle());
    }

    default void appendTooltip(List<Text> tooltip, ItemStack stack) {
        tooltip.add(getCategoryDisplay());
        for (int i = 1; i <= getLineCount(); i++) {
            tooltip.add(getIntroduction(i, stack.getItem()));
        }
    }

    /**
     * 表示珍宝的类别，包含其特有的样式。
     */
    enum Category implements StringIdentifiable {
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
}
