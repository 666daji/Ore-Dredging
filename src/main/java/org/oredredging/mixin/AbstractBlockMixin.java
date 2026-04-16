package org.oredredging.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;
import org.oredredging.util.DropUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {

    /**
     * 如果方块在本次生成战利品时破碎，则重定向一个新的战利品表。
     *
     * @return 最终的战利品表标识符
     */
    @ModifyVariable(method = "getDrops", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootParams;getLevel()Lnet/minecraft/server/level/ServerLevel;"))
    private ResourceLocation dropCrushed(ResourceLocation lootId, BlockState state, LootParams.Builder builder) {
        if (DropUtil.shouldTrigger(state, builder, DropUtil.CrushType.CRUSHED)) {
            ResourceLocation identifier = ForgeRegistries.BLOCKS.getKey(state.getBlock());

            if (identifier != null) {
                return identifier.withPrefix("crushed/");
            }
        }

        return lootId;
    }

    /**
     * 如果方块在本次生成战利品时破碎并且方块标记为具有额外掉落物的，则拼接两张战利品表。
     *
     * @return 最终生成的战利品
     */
    @ModifyExpressionValue(method = "getDrops", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> dropExtra(ObjectArrayList<ItemStack> original, BlockState state, LootParams.Builder builder) {
        if (DropUtil.shouldTrigger(state, builder, DropUtil.CrushType.EXTRA)) {
            LootParams lootContextParameterSet = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
            ServerLevel serverWorld = lootContextParameterSet.getLevel();
            ResourceLocation identifier = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock())).withPrefix("extra/");
            LootTable lootTable = serverWorld.getServer().getLootData().getLootTable(identifier);

            original.addAll(lootTable.getRandomItems(lootContextParameterSet));
        }

        return original;
    }

    @Inject(method = "spawnAfterBreak", at = @At("RETURN"))
    private void applyCrushed(BlockState state, ServerLevel world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        DropUtil.applyCrushedEffect(state, world, pos);
    }
}