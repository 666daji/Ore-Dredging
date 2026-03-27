package org.oredredging.client.render.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.oredredging.OreDredging;
import org.oredredging.item.MinerBundleItem;

import java.util.*;

@Environment(EnvType.CLIENT)
public class MinerBundleTooltipComponent implements TooltipComponent {
    private static final Identifier SLOT_BACKGROUND = new Identifier(OreDredging.MOD_ID, "textures/gui/bundle/slot_background.png");

    private static final int SLOTS_PER_ROW = 4;
    private static final int SLOT_SIZE = 24;
    private static final int GRID_WIDTH = 96;
    private static final int PADDING = 4;
    private static final int MAX_VISIBLE_SLOTS = 20; // 最多显示 20 个格子（包括“+N”格子）

    private final List<Map.Entry<MinerBundleItem.ItemKey, Integer>> mergedEntries;

    public MinerBundleTooltipComponent(MinerBundleItem.MinerBundleTooltipData data) {
        // 将 Map 转换为 List，保留顺序（这里使用自然的迭代顺序即可）
        this.mergedEntries = new ArrayList<>(data.contents().entrySet());
    }

    @Override
    public int getHeight() {
        int rows = MathHelper.ceilDiv(getVisibleEntryCount(), SLOTS_PER_ROW);
        return rows * SLOT_SIZE + PADDING;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return GRID_WIDTH;
    }

    private int getVisibleEntryCount() {
        return Math.min(MAX_VISIBLE_SLOTS, mergedEntries.size());
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        if (mergedEntries.isEmpty()) return;

        boolean hasMore = mergedEntries.size() > MAX_VISIBLE_SLOTS;
        int totalSlots = hasMore ? MAX_VISIBLE_SLOTS : mergedEntries.size();

        int rows = MathHelper.ceilDiv(totalSlots, SLOTS_PER_ROW);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotIndex = row * SLOTS_PER_ROW + col;
                if (slotIndex >= totalSlots) break;

                int slotX = x + col * SLOT_SIZE;
                int slotY = y + row * SLOT_SIZE;

                if (hasMore && slotIndex == MAX_VISIBLE_SLOTS - 1) {
                    drawExtraItemsCount(slotX, slotY, mergedEntries, MAX_VISIBLE_SLOTS - 1, textRenderer, context);
                } else {
                    Map.Entry<MinerBundleItem.ItemKey, Integer> entry = mergedEntries.get(slotIndex);
                    drawItemEntry(entry, slotX, slotY, context, textRenderer);
                }
            }
        }
    }

    private void drawItemEntry(Map.Entry<MinerBundleItem.ItemKey, Integer> entry, int x, int y, DrawContext context, TextRenderer textRenderer) {
        // 绘制背景
        context.drawTexture(SLOT_BACKGROUND, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);

        // 绘制物品图标（数量为1）
        ItemStack iconStack = entry.getKey().toStack(1);
        context.drawItem(iconStack, x + 4, y + 4);

        // 绘制数量文本
        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.translate(0, 0, 300);

        String countText = String.valueOf(entry.getValue());
        int textWidth = textRenderer.getWidth(countText);
        int textX = x + SLOT_SIZE - 4 - textWidth; // 右对齐，留 4 像素边距
        int textY = y + SLOT_SIZE - 4 - 9; // 底部对齐，留 4 像素边距，9 是文字高度

        context.drawTextWithShadow(textRenderer, countText, textX, textY, 0xFFFFFF);
        matrixStack.pop();
    }

    private void drawExtraItemsCount(int x, int y, List<Map.Entry<MinerBundleItem.ItemKey, Integer>> entries, int visibleCount, TextRenderer textRenderer, DrawContext context) {
        // 计算剩余物品的总数量
        int totalRemaining = 0;
        for (int i = visibleCount; i < entries.size(); i++) {
            totalRemaining += entries.get(i).getValue();
        }

        context.drawTexture(SLOT_BACKGROUND, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        String text = "+" + totalRemaining;
        int textWidth = textRenderer.getWidth(text);
        context.drawTextWithShadow(textRenderer, text, x + (SLOT_SIZE - textWidth) / 2, y + (SLOT_SIZE - 9) / 2, 0xFFFFFF);
    }
}