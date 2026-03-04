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
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.oredredging.OreDredging;
import org.oredredging.config.BundlesData;
import org.oredredging.config.ConvergenceRecipesData;
import org.oredredging.config.ModConfigs;
import org.oredredging.config.framework.ConfigManager;
import org.oredredging.registry.ModEnchantments;

import java.util.*;
import java.util.function.Predicate;

/**
 * 矿工收纳袋 - 可存储多种物品，仅计数不计重量，容量固定，不可嵌套。
 * <p>
 * 该物品支持三种附魔效果：
 * <ul>
 *   <li><b>洞天 (EXPANSION)</b> - 每级提升 50% 的存储容量（乘算，向下取整）。</li>
 *   <li><b>聚拢 (CONVERGENCE)</b> - 袋子内物品发生变化时，自动尝试匹配配置的合成配方，
 *       若满足原料数量则消耗原料并生成产物（无视形状，仅按数量匹配）。</li>
 *   <li><b>收纳 (AUTO_PICKING)</b> - 玩家捡起物品时，若物品允许放入且袋子有空间，则自动收入袋中。</li>
 * </ul>
 */
public class MinerBundleItem extends Item {
    public static final String ITEMS_KEY = "Items";
    private final int baseStorage;

    /**
     * @param settings    物品设置
     * @param baseStorage 基础容量（以组为单位，例如 1 = 64 个物品）
     */
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
     * @param stack 袋子物品栈
     * @return 最大可存储物品总数
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
        // 容量 = 基础容量 × (1.5^附魔等级)，向下取整
        double multiplier = Math.pow(1.5, level);
        return (int) Math.floor(base * multiplier);
    }

    /**
     * 检查袋子是否具有收纳附魔。
     */
    public static boolean hasAutoPicking(ItemStack stack) {
        return stack.getItem() instanceof MinerBundleItem &&
                EnchantmentHelper.getLevel(ModEnchantments.AUTO_PICKING, stack) != 0;
    }

    /**
     * 尝试将物品自动收纳到袋子中（收纳附魔功能）。
     *
     * @param bag    袋子物品栈（会被修改）
     * @param toAdd  要收纳的物品（不会被修改）
     * @param player 相关玩家，用于播放音效
     * @return true 表示成功收纳
     */
    public static boolean tryAutoPickup(ItemStack bag, ItemStack toAdd, PlayerEntity player) {
        if (!(bag.getItem() instanceof MinerBundleItem bundleItem)) return false;
        if (!hasAutoPicking(bag)) return false;
        if (!bundleItem.getAllowedItems().test(toAdd)) return false;
        if (bundleItem.addStack(bag, toAdd, player)) {
            bundleItem.playInsertSound(player);
            return true;
        }
        return false;
    }

    /**
     * 判断袋子是否为空。
     */
    public static boolean isEmpty(ItemStack stack) {
        return getTotalCount(stack) == 0;
    }

    // ============================== NBT 辅助 ==============================

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

    private static int getTotalCount(ItemStack stack) {
        return getItems(stack).stream().mapToInt(ItemStack::getCount).sum();
    }

    // ============================== 核心操作 ==============================

    private boolean canAddStack(ItemStack bag, ItemStack stackToAdd) {
        int currentTotal = getTotalCount(bag);
        int addCount = stackToAdd.getCount();
        return currentTotal + addCount <= getStorage(bag);
    }

    /**
     * 向袋子中添加一个完整的物品堆（全部数量）。
     *
     * @param bag    袋子
     * @param toAdd  要添加的物品堆（不会被修改）
     * @param player 玩家（用于音效和世界访问）
     * @return true 添加成功
     */
    private boolean addStack(ItemStack bag, ItemStack toAdd, PlayerEntity player) {
        if (!getAllowedItems().test(toAdd)) return false;
        if (!canAddStack(bag, toAdd)) return false;

        List<ItemStack> contents = getItems(bag);
        int remaining = toAdd.getCount();

        // 合并到现有堆叠
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

        // 剩余部分创建新堆叠
        while (remaining > 0) {
            int stackSize = Math.min(remaining, toAdd.getMaxCount());
            ItemStack newStack = toAdd.copyWithCount(stackSize);
            contents.add(newStack);
            remaining -= stackSize;
        }

        setItems(bag, contents);
        tryConverge(bag, player); // 触发聚拢合成
        return true;
    }

    /**
     * 从袋子中移除第一个堆叠（整个堆叠）。
     *
     * @return 被移除的堆叠副本，若袋子为空则返回空 Optional
     */
    private Optional<ItemStack> removeStack(ItemStack bag) {
        List<ItemStack> contents = getItems(bag);
        if (contents.isEmpty()) return Optional.empty();

        ItemStack first = contents.remove(0);
        setItems(bag, contents);
        return Optional.of(first.copy());
    }

    /**
     * 清空袋子并返回所有物品的副本。
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

    // ============================== 聚拢附魔逻辑 ==============================

    /**
     * 从配置中获取所有允许自动合成的配方。
     */
    private Set<CraftingRecipe> getConvergenceRecipes(World world) {
        ConvergenceRecipesData config = ConfigManager.get(ModConfigs.CONVERGENCE_RECIPES);
        if (config == null || config.recipes().isEmpty()) {
            return Set.of();
        }

        RecipeManager recipeManager = world.getRecipeManager();
        Set<CraftingRecipe> recipes = new HashSet<>();

        for (Identifier id : config.recipes()) {
            Optional<? extends Recipe<?>> optional = recipeManager.get(id);
            if (optional.isPresent()) {
                Recipe<?> recipe = optional.get();
                if (recipe instanceof CraftingRecipe craftingRecipe &&
                        (craftingRecipe instanceof ShapedRecipe || craftingRecipe instanceof ShapelessRecipe)) {
                    recipes.add(craftingRecipe);
                } else {
                    OreDredging.LOGGER.warn("Convergence recipe {} is not a shaped or shapeless crafting recipe, ignored", id);
                }
            } else {
                OreDredging.LOGGER.warn("Convergence recipe {} not found", id);
            }
        }
        return recipes;
    }

    /**
     * 尝试触发聚拢合成（仅当袋子拥有聚拢附魔时）。
     */
    private void tryConverge(ItemStack bag, PlayerEntity player) {
        if (player == null) return;
        if (EnchantmentHelper.getLevel(ModEnchantments.CONVERGENCE, bag) == 0) return;

        Set<CraftingRecipe> recipes = getConvergenceRecipes(player.getWorld());
        if (recipes.isEmpty()) return;

        int maxIterations = 10;
        boolean crafted;
        int iter = 0;
        do {
            crafted = false;
            for (CraftingRecipe recipe : recipes) {
                if (recipe.isEmpty()) continue;
                if (tryCraftOnce(bag, recipe, player)) {
                    crafted = true;
                    break; // 合成一次后重新扫描所有配方
                }
            }
            iter++;
        } while (crafted && iter < maxIterations);
    }

    /**
     * 尝试使用袋子内的物品合成一次指定的配方（无位置要求，仅按数量匹配）。
     *
     * @return true 表示合成成功
     */
    private boolean tryCraftOnce(ItemStack bag, CraftingRecipe recipe, PlayerEntity player) {
        List<ItemStack> contents = getItems(bag);
        // 展开为单个物品列表，便于匹配
        List<ItemStack> flattened = flattenStacks(contents);

        DefaultedList<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) return false;

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        ItemStack result = recipe.getOutput(registryManager);
        if (result.isEmpty() || !getAllowedItems().test(result)) return false;

        // 容量检查
        int currentTotal = flattened.size();
        int requiredCount = ingredients.size();
        int resultCount = result.getCount();
        int newTotal = currentTotal - requiredCount + resultCount;
        if (newTotal > getStorage(bag)) return false;

        // 尝试匹配原料
        List<Integer> matchedIndices = new ArrayList<>();
        boolean[] used = new boolean[flattened.size()];
        if (matchIngredients(flattened, ingredients, 0, used, matchedIndices)) {
            // 扣除原料
            matchedIndices.sort(Collections.reverseOrder());
            for (int idx : matchedIndices) {
                flattened.remove(idx);
            }
            // 重新压缩为堆叠
            List<ItemStack> newContents = compressStacks(flattened);
            // 添加产物
            mergeStack(newContents, result.copy());
            // 更新袋子
            setItems(bag, newContents);
            playInsertSound(player); // 播放合成音效
            return true;
        }
        return false;
    }

    /**
     * 将堆叠列表展开为单个物品列表（每个物品独立为 1 个单位的堆叠）。
     */
    private List<ItemStack> flattenStacks(List<ItemStack> stacks) {
        List<ItemStack> flattened = new ArrayList<>();
        for (ItemStack stack : stacks) {
            for (int i = 0; i < stack.getCount(); i++) {
                flattened.add(stack.copyWithCount(1));
            }
        }
        return flattened;
    }

    /**
     * 回溯匹配：从展开的物品列表中找出能匹配所有原料的物品索引。
     */
    private boolean matchIngredients(List<ItemStack> items, List<Ingredient> ingredients,
                                     int ingIndex, boolean[] used, List<Integer> matchedIndices) {
        if (ingIndex == ingredients.size()) return true;
        Ingredient ing = ingredients.get(ingIndex);
        for (int i = 0; i < items.size(); i++) {
            if (!used[i] && ing.test(items.get(i))) {
                used[i] = true;
                matchedIndices.add(i);
                if (matchIngredients(items, ingredients, ingIndex + 1, used, matchedIndices)) {
                    return true;
                }
                matchedIndices.remove(matchedIndices.size() - 1);
                used[i] = false;
            }
        }
        return false;
    }

    /**
     * 将单个物品列表重新压缩为堆叠列表（尽可能合并相同物品）。
     */
    private List<ItemStack> compressStacks(List<ItemStack> items) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack item : items) {
            boolean merged = false;
            for (ItemStack stack : result) {
                if (ItemStack.canCombine(stack, item)) {
                    int maxCount = stack.getMaxCount();
                    if (stack.getCount() < maxCount) {
                        stack.increment(1);
                        merged = true;
                        break;
                    }
                }
            }
            if (!merged) {
                result.add(item.copy());
            }
        }
        return result;
    }

    /**
     * 将一个物品堆叠合并到已有的堆叠列表中（尽可能填满现有堆叠）。
     */
    private void mergeStack(List<ItemStack> stacks, ItemStack toAdd) {
        int remaining = toAdd.getCount();
        for (ItemStack stack : stacks) {
            if (ItemStack.canCombine(stack, toAdd)) {
                int maxCount = stack.getMaxCount();
                int space = maxCount - stack.getCount();
                if (space > 0) {
                    int merge = Math.min(space, remaining);
                    stack.increment(merge);
                    remaining -= merge;
                    if (remaining == 0) return;
                }
            }
        }
        while (remaining > 0) {
            int size = Math.min(remaining, toAdd.getMaxCount());
            ItemStack newStack = toAdd.copyWithCount(size);
            stacks.add(newStack);
            remaining -= size;
        }
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
            // 右键可接受的物品 -> 尝试将槽位整个堆叠放入袋子
            int count = slotStack.getCount();
            ItemStack toAdd = slotStack.copy();
            if (canAddStack(stack, toAdd)) {
                ItemStack taken = slot.takeStackRange(count, count, player);
                if (!taken.isEmpty() && taken.getCount() == count) {
                    boolean added = addStack(stack, taken, player);
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
            int count = otherStack.getCount();
            ItemStack toAdd = otherStack.copy();
            if (canAddStack(stack, toAdd)) {
                otherStack.decrement(count);
                boolean added = addStack(stack, toAdd, player);
                if (added) {
                    playInsertSound(player);
                } else {
                    // 添加失败，恢复光标数量
                    otherStack.increment(count);
                }
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
        List<ItemStack> contents = getItems(stack);
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
        // 青色
        return MathHelper.packRgb(0.2F, 0.8F, 0.8F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        int count = getTotalCount(stack);
        tooltip.add(Text.translatable("item.minerbundle.fullness", count, getStorage(stack)).formatted(Formatting.GRAY));
    }

    // ============================== 物品实体销毁 ==============================

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ItemStack stack = entity.getStack();
        List<ItemStack> contents = getItems(stack);
        if (!contents.isEmpty()) {
            stack.removeSubNbt(ITEMS_KEY); // 防止递归
            ItemUsage.spawnItemContents(entity, contents.stream());
        }
    }

    // ============================== 音效辅助 ==============================

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

    // ============================== 内部工具提示数据类 ==============================

    public record MinerBundleTooltipData(List<ItemStack> contents, int currentCount, int maxCapacity) implements TooltipData {}
}