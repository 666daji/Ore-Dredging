package org.oredredging.client.render.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.oredredging.OreDredging;
import org.oredredging.item.MinerBundleItem;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MinerBundleTooltipComponent implements TooltipComponent {
    private static final Identifier SLOT_BACKGROUND = new Identifier(OreDredging.MOD_ID, "textures/gui/bundle/slot_background.png");

    private static final int SLOTS_PER_ROW = 4;
    private static final int SLOT_SIZE = 24;
    private static final int GRID_WIDTH = 96;
    private static final int PADDING = 4; // 网格与进度条间距

    private final List<ItemStack> contents;

    public MinerBundleTooltipComponent(MinerBundleItem.MinerBundleTooltipData data) {
        this.contents = data.contents();
    }

    @Override
    public int getHeight() {
        return getRows() * SLOT_SIZE + PADDING;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return GRID_WIDTH;
    }

    private int getRows() {
        return MathHelper.ceilDiv(getNumVisibleSlots(), SLOTS_PER_ROW);
    }

    private int getNumVisibleSlots() {
        return Math.min(20, contents.size());
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        // x, y 是 tooltip 系统计算好的左上角位置，我们直接在这个区域绘制
        if (!contents.isEmpty()) {
            drawNonEmpty(x, y, textRenderer, context);
        }
    }

    private void drawNonEmpty(int x, int y, TextRenderer textRenderer, DrawContext context) {
        boolean hasMore = contents.size() > 12;
        List<ItemStack> shown = contents.subList(0, Math.min(12, contents.size()));

        int rows = getRows();
        int slotIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotX = x + col * SLOT_SIZE;
                int slotY = y + row * SLOT_SIZE;

                if (shouldDrawExtraItemsCount(hasMore, col, row)) {
                    int extraCount = contents.stream().skip(12).mapToInt(ItemStack::getCount).sum();
                    drawExtraItemsCount(slotX, slotY, extraCount, textRenderer, context);
                } else if (slotIndex < shown.size()) {
                    drawItem(shown.get(slotIndex++), slotX, slotY, context, textRenderer);
                } else {
                    context.drawTexture(SLOT_BACKGROUND, slotX, slotY, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                }
            }
        }
    }

    // 原版收纳袋在第一个格子显示剩余物品数量（左上角）
    private static boolean shouldDrawExtraItemsCount(boolean hasMore, int col, int row) {
        return hasMore && col == 0 && row == 0;
    }

    private void drawItem(ItemStack stack, int x, int y, DrawContext context, TextRenderer textRenderer) {
        context.drawTexture(SLOT_BACKGROUND, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        context.drawItem(stack, x + 4, y + 4);
        context.drawItemInSlot(textRenderer, stack, x + 4, y + 4);
    }

    private static void drawExtraItemsCount(int x, int y, int count, TextRenderer textRenderer, DrawContext context) {
        context.drawTexture(SLOT_BACKGROUND, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        String text = "+" + count;
        int textWidth = textRenderer.getWidth(text);
        context.drawTextWithShadow(textRenderer, text, x + (SLOT_SIZE - textWidth) / 2, y + (SLOT_SIZE - 9) / 2, 0xFFFFFF);
    }
}