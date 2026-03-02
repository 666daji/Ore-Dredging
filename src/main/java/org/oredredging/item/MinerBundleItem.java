package org.oredredging.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
import org.oredredging.config.BundlesData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;
import org.oredredging.registry.ModEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 一个可存储多种物品的袋子，只计数不计算权重，最大容量固定，不可嵌套。
 * 存储结构为NBT列表，每个元素是一个物品堆（ItemStack）。
 */
public class MinerBundleItem extends Item {
    public static final String ITEMS_KEY = "Items";
    private final int baseStorage;

    /**
     * 构造函数
     * @param settings      物品设置
     * @param baseStorage    最大容量
     */
    public MinerBundleItem(Settings settings, int baseStorage) {
        super(settings.maxCount(1));
        this.baseStorage = baseStorage * 64;
    }

    /**
     * 获取允许放入的物品谓词。
     *
     * @return 允许放入的物品谓词
     */
    public Predicate<ItemStack> getAllowedItems() {
        BundlesData data = ConfigManager.get(ModConfigs.BUNDLES);
        if (data != null) {
            return data.getPredicate(this);
        }

        return stack -> false;
    }

    /**
     * 获取袋子的最终容量。
     *
     * @param stack 袋子
     * @return 最终容量
     */
    public static int getStorage(ItemStack stack) {
        if (!(stack.getItem() instanceof MinerBundleItem bundleItem)) {
            return 0;
        }

        int base = bundleItem.baseStorage;
        int level = EnchantmentHelper.getLevel(ModEnchantments.EXPANSION, stack);
        if (level == 0) {
            return base;
        }

        // 计算 (1.5)^level，然后乘以基础容量，最后向下取整
        double multiplier = Math.pow(1.5, level);
        return (int) Math.floor(base * multiplier);
    }

    // ==================== NBT 辅助方法 ====================

    /**
     * 从袋子的NBT中读取存储的物品列表
     */
    private static List<ItemStack> getItems(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ITEMS_KEY, NbtElement.LIST_TYPE)) {
            return new ArrayList<>();
        }
        NbtList list = nbt.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
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

    // ==================== 核心操作（整堆为单位） ====================

    /**
     * 检查袋子是否有足够容量容纳整个给定的物品堆。
     */
    private boolean canAddStack(ItemStack bag, ItemStack stackToAdd) {
        int currentTotal = getTotalCount(bag);
        int addCount = stackToAdd.getCount();
        return currentTotal + addCount <= getStorage(bag);
    }

    /**
     * 向袋子中添加一个完整的物品堆（全部数量）。
     * @param bag   袋子
     * @param toAdd 要添加的物品堆（不会被修改）
     * @return true 添加成功，false 失败（超出容量或不允许放入）
     */
    private boolean addStack(ItemStack bag, ItemStack toAdd) {
        if (!getAllowedItems().test(toAdd)) return false;
        if (!canAddStack(bag, toAdd)) return false;

        List<ItemStack> contents = getItems(bag);
        int remaining = toAdd.getCount();

        // 优先合并到现有堆叠中
        for (ItemStack existing : contents) {
            if (ItemStack.canCombine(existing, toAdd)) {
                int maxCount = existing.getMaxCount();
                int space = maxCount - existing.getCount();
                if (space > 0) {
                    int merge = Math.min(space, remaining);
                    existing.increment(merge);
                    remaining -= merge;
                    if (remaining == 0) break;
                }
            }
        }

        // 如果还有剩余，创建新的堆叠
        while (remaining > 0) {
            int stackSize = Math.min(remaining, toAdd.getMaxCount());
            ItemStack newStack = toAdd.copyWithCount(stackSize);
            contents.add(newStack);
            remaining -= stackSize;
        }

        setItems(bag, contents);
        return true;
    }

    /**
     * 从袋子中移除一个完整的物品堆（第一个堆叠）。
     * @return 被移除的物品堆（全部数量），若袋子为空则返回空Optional
     */
    private Optional<ItemStack> removeStack(ItemStack bag) {
        List<ItemStack> contents = getItems(bag);
        if (contents.isEmpty()) return Optional.empty();

        ItemStack first = contents.remove(0);
        setItems(bag, contents);
        return Optional.of(first.copy()); // 返回副本，避免外部修改影响袋子
    }

    // ==================== 收纳 ====================

    /**
     * 检查袋子是由含有收纳的附魔。
     *
     * @param stack 要检查的物品堆栈
     * @return 是否含有收纳效果
     */
    public static boolean hasAutoPicking(ItemStack stack) {
        return stack.getItem() instanceof MinerBundleItem &&
                EnchantmentHelper.getLevel(ModEnchantments.AUTO_PICKING, stack) != 0;
    }

    /**
     * 尝试将物品自动收纳到袋子中
     * @param bag    袋子物品栈（会被修改）
     * @param toAdd  要收纳的物品（不会被修改，方法内会复制）
     * @param player 玩家，用于播放音效
     * @return true 表示成功收纳
     */
    public static boolean tryAutoPickup(ItemStack bag, ItemStack toAdd, PlayerEntity player) {
        if (!(bag.getItem() instanceof MinerBundleItem bundleItem)) return false;

        if (!hasAutoPicking(bag)) return false;

        if (!bundleItem.getAllowedItems().test(toAdd)) return false;

        if (bundleItem.addStack(bag, toAdd)) {
            bundleItem.playInsertSound(player);
            return true;
        }

        return false;
    }

    // ==================== 物品交互方法 ====================

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) return false;

        ItemStack slotStack = slot.getStack();
        if (slotStack.isEmpty()) {
            // 右键空槽 -> 尝试从袋子中取出一个完整的堆叠放入槽位
            Optional<ItemStack> removed = removeStack(stack);
            if (removed.isPresent()) {
                ItemStack toInsert = removed.get();
                ItemStack remaining = slot.insertStack(toInsert);
                if (!remaining.isEmpty()) {
                    // 如果槽位放不下（通常不会发生），尝试加回袋子
                    addStack(stack, remaining);
                }
                playRemoveOneSound(player); // 沿用原音效，语义为取出
                return true;
            }
        } else if (getAllowedItems().test(slotStack)) {
            // 右键可接受的物品 -> 尝试将槽位中的整个堆叠放入袋子
            int count = slotStack.getCount();
            ItemStack toAdd = slotStack.copy(); // 复制用于容量检查
            if (canAddStack(stack, toAdd)) {
                ItemStack taken = slot.takeStackRange(count, count, player);
                if (!taken.isEmpty() && taken.getCount() == count) {
                    boolean added = addStack(stack, taken);
                    if (added) {
                        playInsertSound(player);
                    } else {
                        // 添加失败（理论上不会发生），放回槽位
                        slot.insertStack(taken);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT) return false;

        // 手持袋子，右键点击其他物品（光标上有物品）
        if (!otherStack.isEmpty() && getAllowedItems().test(otherStack)) {
            // 尝试将光标上的整个堆叠放入袋子
            int count = otherStack.getCount();
            ItemStack toAdd = otherStack.copy(); // 复制用于容量检查
            if (canAddStack(stack, toAdd)) {
                otherStack.decrement(count); // 从光标移除整个堆叠
                boolean added = addStack(stack, toAdd);
                if (added) {
                    playInsertSound(player);
                } else {
                    // 添加失败（理论上不会发生），恢复光标数量
                    otherStack.increment(count);
                }
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

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        List<ItemStack> contents = getItems(stack);
        int current = getTotalCount(stack);
        return Optional.of(new MinerBundleTooltipData(contents, current, getStorage(stack)));
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

    // ==================== 进度条显示 ====================

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getTotalCount(stack) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int count = getTotalCount(stack);
        return Math.min(1 + 12 * count / getStorage(stack), 13);
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
        tooltip.add(Text.translatable("item.minerbundle.fullness", count, getStorage(stack)).formatted(Formatting.GRAY));
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

    public record MinerBundleTooltipData(List<ItemStack> contents, int currentCount, int maxCapacity) implements TooltipData {}
}