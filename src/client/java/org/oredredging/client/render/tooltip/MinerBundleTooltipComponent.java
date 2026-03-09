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
    private static final int PADDING = 4;
    private static final int MAX_VISIBLE_SLOTS = 20; // 最多显示20个占位格子（包括剩余数量格子）

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
        return Math.min(MAX_VISIBLE_SLOTS, contents.size());
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        if (!contents.isEmpty()) {
            drawNonEmpty(x, y, textRenderer, context);
        }
    }

    private void drawNonEmpty(int x, int y, TextRenderer textRenderer, DrawContext context) {
        boolean hasMore = contents.size() > MAX_VISIBLE_SLOTS;

        // 实际显示的普通物品堆叠（不包含剩余数量格子）
        List<ItemStack> shown;
        if (hasMore) {
            shown = contents.subList(0, MAX_VISIBLE_SLOTS - 1);
        } else {
            shown = contents.subList(0, contents.size());
        }

        int rows = getRows();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotIndexOverall = row * SLOTS_PER_ROW + col;
                int slotX = x + col * SLOT_SIZE;
                int slotY = y + row * SLOT_SIZE;

                // 第一个格子且有多余物品时，绘制剩余数量
                if (hasMore && slotIndexOverall == 0) {
                    int extraCount = contents.stream()
                            .skip(MAX_VISIBLE_SLOTS)
                            .mapToInt(ItemStack::getCount)
                            .sum();
                    drawExtraItemsCount(slotX, slotY, extraCount, textRenderer, context);
                }
                else {
                    // 计算在 shown 列表中的索引
                    int shownIndex = hasMore ? slotIndexOverall - 1 : slotIndexOverall;
                    if (shownIndex >= 0 && shownIndex < shown.size()) {
                        drawItem(shown.get(shownIndex), slotX, slotY, context, textRenderer);
                    }
                }
            }
        }
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