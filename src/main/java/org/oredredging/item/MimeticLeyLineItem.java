package org.oredredging.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MimeticLeyLineItem extends BlockItem implements Cimelia {
    protected final Category category;
    protected final int lineCount;

    public MimeticLeyLineItem(Block block, Settings settings, Category category, int lineCount) {
        super(block, settings.rarity(Rarity.UNCOMMON).fireproof());
        this.category = category;
        this.lineCount = lineCount;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getLineCount() {
        return lineCount;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        appendTooltip(tooltip, stack);
    }
}
