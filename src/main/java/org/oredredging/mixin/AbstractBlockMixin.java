package org.oredredging.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.oredredging.util.DropUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

    /**
     * 如果方块在本次生成战利品时破碎，则重定向一个新的战利品表。
     *
     * @return 最终的战利品表标识符
     */
    @ModifyVariable(method = "getDroppedStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContextParameterSet;getWorld()Lnet/minecraft/server/world/ServerWorld;"))
    private Identifier dropCrushed(Identifier lootId, BlockState state, LootContextParameterSet.Builder builder) {
        if (DropUtil.shouldTrigger(state, builder, DropUtil.CrushType.CRUSHED)) {
            Identifier identifier = Registries.BLOCK.getId(state.getBlock());

            return identifier.withPrefixedPath("crushed/");
        }

        return lootId;
    }

    /**
     * 如果方块在本次生成战利品时破碎并且方块标记为具有额外掉落物的，则拼接两张战利品表。
     *
     * @return 最终生成的战利品
     */
    @ModifyExpressionValue(method = "getDroppedStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> dropExtra(ObjectArrayList<ItemStack> original, BlockState state, LootContextParameterSet.Builder builder) {
        if (DropUtil.shouldTrigger(state, builder, DropUtil.CrushType.EXTRA)) {
            LootContextParameterSet lootContextParameterSet = builder.add(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
            ServerWorld serverWorld = lootContextParameterSet.getWorld();
            Identifier identifier = Registries.BLOCK.getId(state.getBlock()).withPrefixedPath("extra/");
            LootTable lootTable = serverWorld.getServer().getLootManager().getLootTable(identifier);

            original.addAll(lootTable.generateLoot(lootContextParameterSet));
        }

        return original;
    }

    @Inject(method = "onStacksDropped", at = @At("RETURN"))
    private void applyCrushed(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        DropUtil.applyCrushedEffect(state, world, pos);
    }
}