package org.oredredging.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
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
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
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

    public MinerBundleItem(Settings settings, int baseStorage) {
        super(settings.maxCount(1));
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
        int level = EnchantmentHelper.getLevel(ModEnchantments.EXPANSION, bundle);
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
                EnchantmentHelper.getLevel(ModEnchantments.AUTO_PICKING, bundle) != 0;
    }

    /**
     * 获取袋子中所有的物品堆栈。
     * @param bundle 要获取的袋子
     * @return 袋子中所有的物品堆栈
     */
    public static List<ItemStack> getItems(ItemStack bundle) {
        NbtCompound nbt = bundle.getNbt();
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
     * 设置袋子中存储的物品堆栈。
     * @param bundle 要设置的袋子
     * @param items 新的物品堆栈列表
     */
    public static void setItems(ItemStack bundle, List<ItemStack> items) {
        NbtCompound nbt = bundle.getOrCreateNbt();
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
        NbtCompound nbt = bundle.getNbt();
        if (nbt == null || !nbt.contains(ITEMS_KEY, NbtElement.LIST_TYPE)) {
            return map;
        }
        NbtList list = nbt.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound compound = list.getCompound(i);
            ItemStack stack = ItemStack.fromNbt(compound);
            if (!stack.isEmpty()) {
                ItemKey key = new ItemKey(stack);
                map.merge(key, stack.getCount(), Integer::sum);
            }
        }
        return map;
    }

    /**
     * 将计数映射写回袋子的 NBT（转换为 List<ItemStack> 格式）。
     *
     */
    public static void writeContentToBag(ItemStack bundle, Map<ItemKey, Integer> content) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (Map.Entry<ItemKey, Integer> entry : content.entrySet()) {
            ItemKey key = entry.getKey();
            int count = entry.getValue();
            // 按最大堆叠数拆分
            int maxStack = key.item.getMaxCount();
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
    public int addStack(ItemStack bundle, ItemStack toAdd, PlayerEntity player) {
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
            if (ItemStack.canCombine(existing, toAdd)) {
                int maxStack = existing.getMaxCount();
                int space = maxStack - existing.getCount();
                if (space > 0) {
                    int merge = Math.min(space, toAddRemaining);
                    existing.increment(merge);
                    toAddRemaining -= merge;
                }
            }
        }

        // 2. 剩余部分创建新堆叠
        while (toAddRemaining > 0) {
            int stackSize = Math.min(toAddRemaining, toAdd.getMaxCount());
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
        bundle.removeSubNbt(ITEMS_KEY);
        return copy;
    }

    // ============================== 物品交互 ==============================

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) return false;

        ItemStack slotStack = slot.getStack();
        if (slotStack.isEmpty()) {
            // 右键空槽 -> 尝试从袋子取出一个堆叠放入槽位
            Optional<ItemStack> removed = removeStack(stack);
            if (removed.isPresent()) {
                ItemStack toInsert = removed.get();
                ItemStack remaining = slot.insertStack(toInsert);
                if (!remaining.isEmpty()) {
                    // 槽位放不下，剩余部分放回袋子
                    addStack(stack, remaining, player);
                }
                playRemoveOneSound(player);
                return true;
            }
        } else if (getAllowedItems().test(slotStack)) {
            // 右键可接受的物品 -> 尝试将槽位中尽可能多的物品放入袋子
            int maxAdd = getMaxAddable(stack, slotStack);
            if (maxAdd > 0) {
                ItemStack taken = slot.takeStackRange(maxAdd, maxAdd, player);
                if (!taken.isEmpty()) {
                    int added = addStack(stack, taken, player);
                    if (added > 0) {
                        playInsertSound(player);
                    } else {
                        // 理论上不会失败，但若失败则放回槽位
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
            int maxAdd = getMaxAddable(stack, otherStack);
            if (maxAdd > 0) {
                // 尝试将光标上的物品放入袋子，最多 maxAdd 个
                int toAddCount = Math.min(maxAdd, otherStack.getCount()); // 通常 maxAdd <= otherStack.getCount()
                ItemStack toAdd = otherStack.copyWithCount(toAddCount);
                int added = addStack(stack, toAdd, player);
                if (added > 0) {
                    // 实际添加了 added 个，从光标中减去
                    otherStack.decrement(added);
                    playInsertSound(player);
                }
                // 如果 added == 0，光标不变，且无音效
                return true;
            }
        }

        // 空堆栈时尝试取出
        if (otherStack.isEmpty()) {
            Optional<ItemStack> removed = removeStack(stack);
            removed.ifPresent(cursorStackReference::set);
            return true;
        }

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
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 2;
    }

    // ============================== 工具提示与显示 ==============================

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        Map<ItemKey, Integer> contents = buildContentMap(stack);
        int current = getTotalCount(stack);
        return Optional.of(new MinerBundleTooltipData(contents, current, getStorage(stack)));
    }

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
        return MathHelper.packRgb(0.2F, 0.8F, 0.8F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int count = getTotalCount(stack);
        tooltip.add(Text.translatable("item.minerbundle.fullness", count, getStorage(stack)).formatted(Formatting.GRAY));
    }

    // ============================== 物品实体销毁 ==============================

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ItemStack stack = entity.getStack();
        List<ItemStack> contents = getItems(stack);
        if (!contents.isEmpty()) {
            stack.removeSubNbt(ITEMS_KEY);
            ItemUsage.spawnItemContents(entity, contents.stream());
        }
    }

    // ============================== 音效辅助 ==============================

    public void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public List<EnchantmentLevelEntry> modifyList(List<EnchantmentLevelEntry> original, int power, ItemStack stack, boolean treasureAllowed) {
        List<EnchantmentLevelEntry> result = new ArrayList<>(original);
        List<Enchantment> customEnchantments = List.of(
                ModEnchantments.CONVERGENCE,
                ModEnchantments.AUTO_PICKING,
                ModEnchantments.EXPANSION
        );

        for (Enchantment enchantment : customEnchantments) {
            EnchantmentLevelEntry entry = PossibleEnchantment.getBestLevelEntry(enchantment, power);
            if (entry == null) {
                continue;
            }
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
    public record MinerBundleTooltipData(Map<ItemKey, Integer> contents, int currentCount, int maxCapacity) implements TooltipData {}

    /**
     * 物品唯一标识（基于物品和 NBT）
     */
    public static class ItemKey {
        public final Item item;
        public final NbtCompound nbt; // 可能为 null

        public ItemKey(ItemStack stack) {
            this.item = stack.getItem();
            this.nbt = stack.getNbt() != null ? stack.getNbt().copy() : null;
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
                stack.setNbt(nbt.copy());
            }
            return stack;
        }
    }
}