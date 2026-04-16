package org.oredredging.client.render.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.oredredging.OreDredging;
import org.oredredging.item.MinerBundleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MinerBundleTooltipComponent implements ClientTooltipComponent {
    private static final ResourceLocation SLOT_BACKGROUND = OreDredging.createResourceLocation(OreDredging.MOD_ID, "textures/gui/bundle/slot_background.png");

    private static final int SLOTS_PER_ROW = 4;
    private static final int SLOT_SIZE = 24;
    private static final int GRID_WIDTH = 96;
    private static final int PADDING = 4;
    private static final int MAX_VISIBLE_SLOTS = 20; // 最多显示 20 个格子（包括“+N”格子）

    private final List<Map.Entry<MinerBundleItem.ItemKey, Integer>> mergedEntries;

    public MinerBundleTooltipComponent(MinerBundleItem.MinerBundleTooltipData data) {
        // 将 Map 转换为 List，保留顺序
        this.mergedEntries = new ArrayList<>(data.contents().entrySet());
    }

    @Override
    public int getHeight() {
        int rows = Mth.positiveCeilDiv(getVisibleEntryCount(), SLOTS_PER_ROW);
        return rows * SLOT_SIZE + PADDING;
    }

    @Override
    public int getWidth(Font font) {
        return GRID_WIDTH;
    }

    private int getVisibleEntryCount() {
        return Math.min(MAX_VISIBLE_SLOTS, mergedEntries.size());
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        if (mergedEntries.isEmpty()) return;

        boolean hasMore = mergedEntries.size() > MAX_VISIBLE_SLOTS;
        int totalSlots = hasMore ? MAX_VISIBLE_SLOTS : mergedEntries.size();

        int rows = Mth.positiveCeilDiv(totalSlots, SLOTS_PER_ROW);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotIndex = row * SLOTS_PER_ROW + col;
                if (slotIndex >= totalSlots) break;

                int slotX = x + col * SLOT_SIZE;
                int slotY = y + row * SLOT_SIZE;

                if (hasMore && slotIndex == MAX_VISIBLE_SLOTS - 1) {
                    drawExtraItemsCount(slotX, slotY, mergedEntries, MAX_VISIBLE_SLOTS - 1, font, graphics);
                } else {
                    Map.Entry<MinerBundleItem.ItemKey, Integer> entry = mergedEntries.get(slotIndex);
                    drawItemEntry(entry, slotX, slotY, graphics, font);
                }
            }
        }
    }

    private void drawItemEntry(Map.Entry<MinerBundleItem.ItemKey, Integer> entry, int x, int y, GuiGraphics graphics, Font font) {
        // 绘制背景
        graphics.blit(SLOT_BACKGROUND, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);

        // 绘制物品图标（数量为1）
        ItemStack iconStack = entry.getKey().toStack(1);
        graphics.renderItem(iconStack, x + 4, y + 4);

        // 绘制数量文本
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 300);

        String countText = String.valueOf(entry.getValue());
        int textWidth = font.width(countText);
        int textX = x + SLOT_SIZE - 4 - textWidth; // 右对齐，留 4 像素边距
        int textY = y + SLOT_SIZE - 4 - 9; // 底部对齐，留 4 像素边距，9 是文字高度

        graphics.drawString(font, countText, textX, textY, 0xFFFFFF, true);
        poseStack.popPose();
    }

    private void drawExtraItemsCount(int x, int y, List<Map.Entry<MinerBundleItem.ItemKey, Integer>> entries, int visibleCount, Font font, GuiGraphics graphics) {
        // 计算剩余物品的总数量
        int totalRemaining = 0;
        for (int i = visibleCount; i < entries.size(); i++) {
            totalRemaining += entries.get(i).getValue();
        }

        graphics.blit(SLOT_BACKGROUND, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        String text = "+" + totalRemaining;
        int textWidth = font.width(text);
        graphics.drawString(font, text, x + (SLOT_SIZE - textWidth) / 2, y + (SLOT_SIZE - 9) / 2, 0xFFFFFF, true);
    }
}