package org.oredredging.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.oredredging.config.BundlesData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;
import org.oredredging.enchantment.MinerBundleEnchantment;
import org.oredredging.registry.ModEnchantments;

import java.util.*;
import java.util.function.Predicate;

public class MinerBundleItem extends Item implements PossibleEnchantment {
    public static final String ITEMS_KEY = "Items";
    private final int baseStorage;

    public MinerBundleItem(Properties properties, int baseStorage) {
        super(properties.stacksTo(1));
        this.baseStorage = baseStorage * 64;
    }

    // ============================== 公共 API ==============================

    /**
     * 获取袋子允许存储的物品谓词（通过配置定义）。
     */
    public Predicate<ItemStack> getAllowedItems() {
        BundlesData data = ConfigManager.get(ModConfigs.BUNDLES);
        if (data != null) {
            return data.getPredicate(this);
        }
        return stack -> false;
    }

    /**
     * 计算袋子的最终容量（考虑洞天附魔加成）。
     *
     * @param bundle 袋子物品栈
     * @return 最大可存储物品总数
     */
    public static int getStorage(ItemStack bundle) {
        if (!(bundle.getItem() instanceof MinerBundleItem bundleItem)) {
            return 0;
        }
        int base = bundleItem.baseStorage;
        int level = EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.EXPANSION.get(), bundle);
        if (level == 0) {
            return base;
        }
        // 容量 = 基础容量 × (1.5^附魔等级)，向下取整
        double multiplier = Math.pow(1.5, level);
        return (int) Math.floor(base * multiplier);
    }

    /**
     * 检查袋子是否具有收纳附魔。
     */
    public static boolean hasAutoPicking(ItemStack bundle) {
        return bundle.getItem() instanceof MinerBundleItem &&
                EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.AUTO_PICKING.get(), bundle) != 0;
    }

    /**
     * 获取袋子中所有的物品堆栈。
     * @param bundle 要获取的袋子
     * @return 袋子中所有的物品堆栈
     */
    public static List<ItemStack> getItems(ItemStack bundle) {
        CompoundTag nbt = bundle.getTag();
        if (nbt == null || !nbt.contains(ITEMS_KEY, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        ListTag list = nbt.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            ItemStack itemStack = ItemStack.of(compound);
            if (!itemStack.isEmpty()) {
                items.add(itemStack);
            }
        }
        return items;
    }

    /**
     * 设置袋子中存储的物品堆栈。
     * @param bundle 要设置的袋子
     * @param items 新的物品堆栈列表
     */
    public static void setItems(ItemStack bundle, List<ItemStack> items) {
        CompoundTag nbt = bundle.getOrCreateTag();
        ListTag list = new ListTag();
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                CompoundTag compound = new CompoundTag();
                item.save(compound);
                list.add(compound);
            }
        }
        nbt.put(ITEMS_KEY, list);
    }

    /**
     * 获取袋子中物品的总占用容量。
     * @param bundle 要获取的袋子
     * @return 袋子已经消耗的容量
     */
    public static int getTotalCount(ItemStack bundle) {
        return getItems(bundle).stream().mapToInt(ItemStack::getCount).sum();
    }

    /**
     * 检查袋子是否为空
     * @param bundle 要检查的袋子
     * @return 袋子是否为空
     */
    public static boolean isEmpty(ItemStack bundle) {
        return getTotalCount(bundle) == 0;
    }

    /**
     * 从袋子的 NBT 构建计数映射。
     *
     * @param bundle 要操作的袋子
     * @return 袋子对应的计数映射
     */
    public static Map<ItemKey, Integer> buildContentMap(ItemStack bundle) {
        Map<ItemKey, Integer> map = new HashMap<>();
        CompoundTag nbt = bundle.getTag();
        if (nbt == null || !nbt.contains(ITEMS_KEY, Tag.TAG_LIST)) {
            return map;
        }
        ListTag list = nbt.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            ItemStack stack = ItemStack.of(compound);
            if (!stack.isEmpty()) {
                ItemKey key = new ItemKey(stack);
                map.merge(key, stack.getCount(), Integer::sum);
            }
        }
        return map;
    }

    /**
     * 将计数映射写回袋子的 NBT（转换为 List<ItemStack> 格式）。
     */
    public static void writeContentToBag(ItemStack bundle, Map<ItemKey, Integer> content) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (Map.Entry<ItemKey, Integer> entry : content.entrySet()) {
            ItemKey key = entry.getKey();
            int count = entry.getValue();
            // 按最大堆叠数拆分
            int maxStack = key.item.getMaxStackSize();
            while (count > 0) {
                int size = Math.min(count, maxStack);
                ItemStack stack = key.toStack(size);
                itemStacks.add(stack);
                count -= size;
            }
        }
        setItems(bundle, itemStacks);
    }

    // ============================== 核心操作 ==============================

    /**
     * 计算当前袋子还能添加多少个指定物品（考虑类型限制和剩余容量）。
     *
     * @param bundle 袋子
     * @param toAdd  待添加的物品
     * @return 最多可以添加的数量
     */
    public int getMaxAddable(ItemStack bundle, ItemStack toAdd) {
        if (!getAllowedItems().test(toAdd)) return 0;
        int currentTotal = getTotalCount(bundle);
        int maxCapacity = getStorage(bundle);
        int remainingSpace = maxCapacity - currentTotal;
        return Math.min(toAdd.getCount(), remainingSpace);
    }

    /**
     * 向袋子中添加物品（尽可能多）。
     *
     * @param bundle 袋子
     * @param toAdd  要添加的物品堆（不会被修改）
     * @param player 玩家（用于音效和世界访问）
     * @return 实际添加的数量
     */
    public int addStack(ItemStack bundle, ItemStack toAdd, Player player) {
        if (!getAllowedItems().test(toAdd)) return 0;

        List<ItemStack> contents = getItems(bundle);
        int currentTotal = contents.stream().mapToInt(ItemStack::getCount).sum();
        int maxCapacity = getStorage(bundle);
        int remainingSpace = maxCapacity - currentTotal;
        if (remainingSpace <= 0) return 0;

        int addCount = Math.min(toAdd.getCount(), remainingSpace);
        if (addCount == 0) return 0;

        int toAddRemaining = addCount;

        // 1. 优先合并到现有堆叠
        for (ItemStack existing : contents) {
            if (toAddRemaining == 0) break;
            if (ItemStack.isSameItemSameTags(existing, toAdd)) {
                int maxStack = existing.getMaxStackSize();
                int space = maxStack - existing.getCount();
                if (space > 0) {
                    int merge = Math.min(space, toAddRemaining);
                    existing.grow(merge);
                    toAddRemaining -= merge;
                }
            }
        }

        // 2. 剩余部分创建新堆叠
        while (toAddRemaining > 0) {
            int stackSize = Math.min(toAddRemaining, toAdd.getMaxStackSize());
            ItemStack newStack = toAdd.copyWithCount(stackSize);
            contents.add(newStack);
            toAddRemaining -= stackSize;
        }

        setItems(bundle, contents);
        // 触发聚拢合成
        MinerBundleEnchantment.triggerConverge(bundle, player);
        return addCount;
    }

    /**
     * 从袋子中移除第一个堆叠（整个堆叠）。
     *
     * @return 被移除的堆叠副本，若袋子为空则返回空 Optional
     */
    private Optional<ItemStack> removeStack(ItemStack bundle) {
        List<ItemStack> contents = getItems(bundle);
        if (contents.isEmpty()) return Optional.empty();

        ItemStack first = contents.remove(0);
        setItems(bundle, contents);
        return Optional.of(first.copy());
    }

    /**
     * 清空袋子并返回所有物品的副本。
     */
    private List<ItemStack> removeAll(ItemStack bundle) {
        List<ItemStack> contents = getItems(bundle);
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack stack : contents) {
            copy.add(stack.copy());
        }
        bundle.removeTagKey(ITEMS_KEY);
        return copy;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess cursorSlotAccess) {
        if (action != ClickAction.SECONDARY) return false;

        // 情况1：光标上有物品（otherStack 非空）且允许放入袋子
        if (!otherStack.isEmpty() && getAllowedItems().test(otherStack)) {
            int maxAdd = getMaxAddable(stack, otherStack);
            if (maxAdd > 0) {
                int toAddCount = Math.min(maxAdd, otherStack.getCount());
                ItemStack toAdd = otherStack.copyWithCount(toAddCount);
                int added = addStack(stack, toAdd, player);
                if (added > 0) {
                    otherStack.shrink(added);
                    playInsertSound(player);
                }
                return true;
            }
        }

        // 情况2：光标为空，尝试从袋子取出一个堆叠
        if (otherStack.isEmpty()) {
            Optional<ItemStack> removed = removeStack(stack);
            if (removed.isPresent()) {
                cursorSlotAccess.set(removed.get());
                playRemoveOneSound(player);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) {
            // 目标槽位为空：尝试从袋子中取出一个堆叠放入目标槽位
            Optional<ItemStack> removed = removeStack(stack);
            if (removed.isPresent()) {
                ItemStack toInsert = removed.get();
                ItemStack remaining = slot.safeInsert(toInsert);
                if (!remaining.isEmpty()) {
                    // 槽位放不下，剩余部分放回袋子
                    addStack(stack, remaining, player);
                }
                playRemoveOneSound(player);
                return true;
            }
        } else if (getAllowedItems().test(slotStack)) {
            // 目标槽位有可接受的物品：尝试将槽位中的物品移入袋子
            int maxAdd = getMaxAddable(stack, slotStack);
            if (maxAdd > 0) {
                ItemStack taken = slot.remove(maxAdd);
                if (!taken.isEmpty()) {
                    int added = addStack(stack, taken, player);
                    if (added > 0) {
                        playInsertSound(player);
                    } else {
                        // 理论上不会失败，但若失败则放回槽位
                        slot.set(taken);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isEmpty(stack)) {
            // 右键空气时，丢弃所有内容
            List<ItemStack> contents = removeAll(stack);
            if (!level.isClientSide) {
                for (ItemStack item : contents) {
                    player.drop(item, true);
                }
            }
            playDropContentsSound(player);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 2;
    }

    // ============================== 工具提示与显示 ==============================

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int count = getTotalCount(stack);
        tooltip.add(Component.translatable("item.minerbundle.fullness", count, getStorage(stack)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getTotalCount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int count = getTotalCount(stack);
        int max = getStorage(stack);
        return Math.min(1 + 12 * count / max, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xA0A0FF;  // 浅蓝色
    }

    // ============================== 物品实体销毁 ==============================

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        List<ItemStack> contents = getItems(stack);
        if (!contents.isEmpty()) {
            stack.removeTagKey(ITEMS_KEY);
            // 生成内容物实体
            for (ItemStack content : contents) {
                itemEntity.level().addFreshEntity(new ItemEntity(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), content));
            }
        }
    }

    // ============================== 音效辅助 ==============================

    public void playRemoveOneSound(Entity entity) {
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 0.8F,
                0.8F + entity.level().random.nextFloat() * 0.4F);
    }

    public void playInsertSound(Entity entity) {
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F,
                0.8F + entity.level().random.nextFloat() * 0.4F);
    }

    public void playDropContentsSound(Entity entity) {
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F,
                0.8F + entity.level().random.nextFloat() * 0.4F);
    }

    @Override
    public List<EnchantmentInstance> modifyList(List<EnchantmentInstance> original, int power, ItemStack stack, boolean treasureAllowed) {
        List<EnchantmentInstance> result = new ArrayList<>(original);
        List<Enchantment> customEnchantments = List.of(
                ModEnchantments.CONVERGENCE.get(),
                ModEnchantments.AUTO_PICKING.get(),
                ModEnchantments.EXPANSION.get()
        );

        for (Enchantment enchantment : customEnchantments) {
            EnchantmentInstance entry = PossibleEnchantment.getBestLevelEntry(enchantment, power);
            if (entry == null) continue;
            result.add(entry);
        }

        return result;
    }

    /**
     * 物品组件数据。
     *
     * @param contents 总物品计数
     * @param currentCount 当前数量
     * @param maxCapacity 最大容量
     */
    public record MinerBundleTooltipData(Map<ItemKey, Integer> contents, int currentCount, int maxCapacity) implements TooltipComponent {}

    /**
     * 物品唯一标识（基于物品和 NBT）
     */
    public static class ItemKey {
        public final Item item;
        public final CompoundTag nbt; // 可能为 null

        public ItemKey(ItemStack stack) {
            this.item = stack.getItem();
            this.nbt = stack.getTag() != null ? stack.getTag().copy() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemKey key = (ItemKey) o;
            return item.equals(key.item) && Objects.equals(nbt, key.nbt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, nbt);
        }

        public ItemStack toStack(int count) {
            ItemStack stack = new ItemStack(item, count);
            if (nbt != null) {
                stack.setTag(nbt.copy());
            }
            return stack;
        }
    }
}