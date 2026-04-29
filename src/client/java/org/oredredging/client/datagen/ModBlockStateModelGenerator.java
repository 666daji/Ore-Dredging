package org.oredredging.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.oredredging.registry.ModBlocks;

import java.util.Optional;

public class ModBlockStateModelGenerator extends FabricModelProvider {
    public ModBlockStateModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // 粗金属块半砖
        registerSlab(blockStateModelGenerator, ModBlocks.RAW_IRON_SLAB, Blocks.RAW_IRON_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.RAW_COPPER_SLAB, Blocks.RAW_COPPER_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.RAW_GOLD_SLAB, Blocks.RAW_GOLD_BLOCK);

        // 金属块半砖
        registerSlab(blockStateModelGenerator, ModBlocks.IRON_SLAB, Blocks.IRON_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.GOLD_SLAB, Blocks.GOLD_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.COPPER_SLAB, Blocks.COPPER_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.NETHERITE_SLAB, Blocks.NETHERITE_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.DIAMOND_SLAB, Blocks.DIAMOND_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.EMERALD_SLAB, Blocks.EMERALD_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.LAPIS_SLAB, Blocks.LAPIS_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.REDSTONE_SLAB, Blocks.REDSTONE_BLOCK);
        registerSlab(blockStateModelGenerator, ModBlocks.COAL_SLAB, Blocks.COAL_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // 粗金属块半砖物品模型
        registerSlabItemModel(itemModelGenerator, ModBlocks.RAW_IRON_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.RAW_COPPER_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.RAW_GOLD_SLAB.asItem());

        // 金属块半砖物品模型
        registerSlabItemModel(itemModelGenerator, ModBlocks.IRON_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.GOLD_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.COPPER_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.NETHERITE_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.DIAMOND_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.EMERALD_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.LAPIS_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.REDSTONE_SLAB.asItem());
        registerSlabItemModel(itemModelGenerator, ModBlocks.COAL_SLAB.asItem());
    }

    /**
     * 为半砖注册方块状态和模型
     * @param generator 生成器实例
     * @param slab 半砖方块
     * @param fullBlock 对应的完整块（用于纹理引用）
     */
    private void registerSlab(BlockStateModelGenerator generator, Block slab, Block fullBlock) {
        Identifier slabId = Registries.BLOCK.getId(slab);
        String slabPath = slabId.getPath();  // 例如 "coal_slab"

        // 自定义模型ID： ore_dredging:block/coal_slab_bottom , ore_dredging:block/coal_slab_top
        Identifier bottomModelId = Identifier.of(OreDredging.MOD_ID, "block/" + slabPath + "_bottom");
        Identifier topModelId    = Identifier.of(OreDredging.MOD_ID, "block/" + slabPath + "_top");

        // 完整块的模型ID（由原版或模组提供）
        Identifier fullBlockModelId = ModelIds.getBlockModelId(fullBlock);

        // 使用完整块的命名空间和路径构造纹理 ID
        Identifier fullBlockId = Registries.BLOCK.getId(fullBlock);
        Identifier fullBlockTextureId = Identifier.of(
                fullBlockId.getNamespace(),
                "block/" + fullBlockId.getPath()
        );

        // 注册底部半砖模型（使用 Models.SLAB）
        registerSimpleModel(generator, bottomModelId, Models.SLAB, fullBlockTextureId);
        // 注册顶部半砖模型（使用 Models.SLAB_TOP）
        registerSimpleModel(generator, topModelId, Models.SLAB_TOP, fullBlockTextureId);

        // 构建方块状态
        generator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(slab)
                        .coordinate(BlockStateVariantMap.create(Properties.SLAB_TYPE)
                                .register(SlabType.BOTTOM, BlockStateVariant.create().put(VariantSettings.MODEL, bottomModelId))
                                .register(SlabType.TOP,    BlockStateVariant.create().put(VariantSettings.MODEL, topModelId))
                                .register(SlabType.DOUBLE, BlockStateVariant.create().put(VariantSettings.MODEL, fullBlockModelId))
                        )
        );
    }

    /**
     * 辅助方法：把一个简单模型（如 slab、slab_top）及其纹理直接注册到 data generator 的模型收集器中。
     */
    private void registerSimpleModel(BlockStateModelGenerator generator, Identifier modelId, Model model, Identifier textureId) {
        model.upload(modelId, TextureMap.all(textureId), generator.modelCollector);
    }

    /**
     * 为半砖物品注册模型，使其继承对应的方块模型。
     * 例如：ore_dredging:item/raw_iron_slab.json -> { "parent": "ore_dredging:block/raw_iron_slab" }
     */
    private void registerSlabItemModel(ItemModelGenerator generator, Item slabItem) {
        Identifier blockModelId = Identifier.of(OreDredging.MOD_ID, "block/" + getItemPath(slabItem));
        if (blockModelId != null) {
            Model itemModel = new Model(Optional.of(blockModelId), Optional.empty());
            generator.register(slabItem, itemModel);
        }
    }

    private String getItemPath(Item item) {
        return Registries.ITEM.getId(item).getPath();
    }
}