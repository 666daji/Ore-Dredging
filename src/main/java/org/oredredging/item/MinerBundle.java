package org.oredredging.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 一个可存储多种物品的袋子，只计数不计算权重，最大容量固定，不可嵌套。
 * 存储结构为NBT列表，每个元素是一个物品堆（ItemStack）。
 */
public class MinerBundle extends Item {
    public static final String ITEMS_KEY = "Items";          // NBT中存储物品列表的键
    private final int maxStorage;                             // 最大物品个数
    private final Predicate<ItemStack> allowedItems;         // 允许放入的物品谓词

    /**
     * 构造函数
     * @param settings      物品设置
     * @param maxStorage    最大容量（物品个数）
     * @param allowedItems  允许放入的物品判断逻辑
     */
    public MinerBundle(Settings settings, int maxStorage, Predicate<ItemStack> allowedItems) {
        super(settings.maxCount(1)); // 袋子本身不可堆叠，无论空满
        this.maxStorage = maxStorage;
        this.allowedItems = allowedItems;
    }

    // ==================== NBT 辅助方法 ====================

    /**
     * 从袋子的NBT中读取存储的物品列表
     */
    private static List<ItemStack> getItems(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ITEMS_KEY, 9)) { // 9 表示 NbtList 类型
            return new ArrayList<>();
        }
        NbtList list = nbt.getList(ITEMS_KEY, 10); // 10 表示 NbtCompound
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NbtCompound compound = list.getCompound(i);
            ItemStack itemStack = ItemStack.fromNbt(compound);
            if (!itemStack.isEmpty()) {
                items.add(itemStack);
            }
        }
        return items;
    }

    /**
     * 将物品列表写回袋子的NBT
     */
    private static void setItems(ItemStack stack, List<ItemStack> items) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList list = new NbtList();
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                NbtCompound compound = new NbtCompound();
                item.writeNbt(compound);
                list.add(compound);
            }
        }
        nbt.put(ITEMS_KEY, list);
    }

    /**
     * 获取当前袋子中的物品总个数
     */
    private static int getTotalCount(ItemStack stack) {
        List<ItemStack> items = getItems(stack);
        return items.stream().mapToInt(ItemStack::getCount).sum();
    }

    /**
     * 判断袋子是否为空
     */
    public static boolean isEmpty(ItemStack stack) {
        return getTotalCount(stack) == 0;
    }

    // ==================== 核心操作 ====================

    /**
     * 向袋子中添加一个物品（数量为1）
     * @param bag   袋子
     * @param item  要添加的物品（假设数量为1，但方法会取其一个）
     * @return true 添加成功，false 失败（超出容量或不允许放入）
     */
    private boolean addOne(ItemStack bag, ItemStack item) {
        if (!allowedItems.test(item)) return false;

        List<ItemStack> contents = getItems(bag);
        int currentTotal = contents.stream().mapToInt(ItemStack::getCount).sum();
        if (currentTotal >= maxStorage) return false; // 已满

        // 尝试合并到现有堆中
        ItemStack single = item.copyWithCount(1); // 只取一个
        for (ItemStack existing : contents) {
            if (ItemStack.canCombine(existing, single)) {
                int maxCount = existing.getMaxCount();
                if (existing.getCount() < maxCount) {
                    existing.increment(1);
                    setItems(bag, contents);
                    return true;
                }
            }
        }

        // 无法合并，创建新堆
        contents.add(single.copy());
        setItems(bag, contents);
        return true;
    }

    /**
     * 从袋子中移除一个物品（从第一个堆中取一个）
     * @return 被移除的物品堆（数量1），若袋子为空则返回空Optional
     */
    private Optional<ItemStack> removeOne(ItemStack bag) {
        List<ItemStack> contents = getItems(bag);
        if (contents.isEmpty()) return Optional.empty();

        ItemStack first = contents.get(0);
        if (first.getCount() > 1) {
            // 拆分一个
            first.decrement(1);
            ItemStack removed = first.copyWithCount(1);
            setItems(bag, contents);
            return Optional.of(removed);
        } else {
            // 移除整个堆
            ItemStack removed = first.copy();
            contents.remove(0);
            setItems(bag, contents);
            return Optional.of(removed);
        }
    }

    /**
     * 清空袋子，返回所有物品的列表（复制）
     */
    private List<ItemStack> removeAll(ItemStack bag) {
        List<ItemStack> contents = getItems(bag);
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack stack : contents) {
            copy.add(stack.copy());
        }
        bag.removeSubNbt(ITEMS_KEY);
        return copy;
    }

    // ==================== 物品交互方法 ====================

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) return false;

        ItemStack slotStack = slot.getStack();
        if (slotStack.isEmpty()) {
            // 右键空槽 -> 尝试取出一件放入槽位
            Optional<ItemStack> removed = removeOne(stack);
            if (removed.isPresent()) {
                ItemStack remaining = slot.insertStack(removed.get());
                if (!remaining.isEmpty()) {
                    // 如果槽位放不下（通常不会发生），尝试加回袋子
                    addOne(stack, remaining);
                }
                playRemoveOneSound(player);
                return true;
            }
        } else if (allowedItems.test(slotStack)) {
            // 右键可接受的物品 -> 尝试从槽位取一件放入袋子
            // 从槽位取一个物品（需要检查槽位是否允许）
            ItemStack toAdd = slot.takeStackRange(1, 1, player);
            if (!toAdd.isEmpty()) {
                boolean added = addOne(stack, toAdd);
                if (added) {
                    playInsertSound(player);
                } else {
                    // 添加失败，放回槽位
                    slot.insertStack(toAdd);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT) return false;

        // 手持袋子，右键点击其他物品（光标上有物品）
        if (!otherStack.isEmpty() && allowedItems.test(otherStack)) {
            // 尝试从光标取一件放入袋子
            ItemStack toAdd = otherStack.copyWithCount(1);
            boolean added = addOne(stack, toAdd);
            if (added) {
                otherStack.decrement(1);
                playInsertSound(player);
                return true;
            }
        }
        // 此处不处理右键空光标取出所有（保持简单）
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!isEmpty(stack)) {
            // 右键空气时，丢弃所有内容
            List<ItemStack> contents = removeAll(stack);
            if (!world.isClient) {
                for (ItemStack item : contents) {
                    user.dropItem(item, true);
                }
            }
            playDropContentsSound(user);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(stack, world.isClient());
        }
        return TypedActionResult.pass(stack);
    }

    // ==================== 进度条显示 ====================

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getTotalCount(stack) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int count = getTotalCount(stack);
        return Math.min(1 + 12 * count / maxStorage, 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        // 自定义颜色，例如青色
        return MathHelper.packRgb(0.2F, 0.8F, 0.8F);
    }

    // ==================== 工具提示 ====================

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        int count = getTotalCount(stack);
        tooltip.add(Text.translatable("item.minerbundle.fullness", count, maxStorage).formatted(Formatting.GRAY));

        List<ItemStack> contents = getItems(stack);
        if (!contents.isEmpty()) {
            tooltip.add(Text.translatable("item.minerbundle.contains").formatted(Formatting.GRAY));
            // 显示前5个物品
            int shown = 0;
            for (ItemStack item : contents) {
                if (shown >= 5) {
                    tooltip.add(Text.literal(" ...").formatted(Formatting.GRAY));
                    break;
                }
                tooltip.add(Text.literal("  ").append(item.getName()).append(" x" + item.getCount()).formatted(Formatting.DARK_GRAY));
                shown++;
            }
        } else {
            tooltip.add(Text.translatable("item.minerbundle.empty").formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
        }
    }

    // ==================== 物品实体销毁时散落内容 ====================

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ItemStack stack = entity.getStack();
        List<ItemStack> contents = getItems(stack);
        if (!contents.isEmpty()) {
            stack.removeSubNbt(ITEMS_KEY); // 防止递归
            ItemUsage.spawnItemContents(entity, contents.stream());
        }
    }

    // ==================== 音效辅助 ====================

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }
}