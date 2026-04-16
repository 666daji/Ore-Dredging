package org.oredredging.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CimeliaItem extends Item {
    public static final Style INTRODUCTION_STYLE;
    public static final String INTRODUCTION_PREFIX = "ore_dredging.cimelia_introduction.";

    protected final Category category;
    protected String introductionKey;
    protected final int lineCount;

    public CimeliaItem(Properties properties, Category category, int lineCount) {
        super(properties.rarity(Rarity.UNCOMMON).fireResistant());
        this.category = category;
        this.lineCount = lineCount;
    }

    /**
     * 获取显示名称后缀，包含类别样式。
     */
    protected Component getCategoryDisplay() {
        return Component.translatable("ore_dredging.cimelia." + this.category.getSerializedName())
                .withStyle(category.getDisplayStyle());
    }

    /**
     * 获取指定行数的简介文本。
     */
    public Component getIntroduction(int line) {
        if (introductionKey == null) {
            String path = ForgeRegistries.ITEMS.getKey(this).getPath();
            introductionKey = INTRODUCTION_PREFIX + path;
        }
        return Component.translatable(introductionKey + "_" + line).withStyle(INTRODUCTION_STYLE);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(getCategoryDisplay());
        for (int i = 1; i <= lineCount; i++) {
            tooltip.add(getIntroduction(i));
        }
    }

    /**
     * 珍宝类别
     */
    public enum Category {
        NATURE("nature", Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(true)),
        ANCIENT("ancient", Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(true));

        private final String name;
        private final Style displayStyle;

        Category(String name, Style displayStyle) {
            this.name = name;
            this.displayStyle = displayStyle;
        }

        public String getSerializedName() {
            return name;
        }

        public Style getDisplayStyle() {
            return displayStyle;
        }
    }

    static {
        INTRODUCTION_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x9A3C78)).withItalic(true);
    }
}