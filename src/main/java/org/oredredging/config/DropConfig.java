package org.oredredging.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.oredredging.OreDredging;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理破碎掉落配置文件 (crushedDrops.json) 的加载与保存。
 */
public class DropConfig {
    private static final Logger LOGGER = OreDredging.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 配置文件路径
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("tw_ore_dredging");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("crushedDrops.json");

    // 默认方块 ID 列表
    private static final List<String> DEFAULT_CAN_CRUSHED = List.of(
            "minecraft:stone",
            "minecraft:sandstone",
            "minecraft:sand",
            "minecraft:deepslate",
            "minecraft:andesite",
            "minecraft:diorite",
            "minecraft:granite",
            "minecraft:gravel"
    );

    private static final List<String> DEFAULT_HAVE_EXTRA = List.of(
            "minecraft:gold_ore",
            "minecraft:deepslate_gold_ore",
            "minecraft:copper_ore",
            "minecraft:deepslate_copper_ore",
            "minecraft:iron_ore",
            "minecraft:deepslate_iron_ore"
    );

    // 当前加载的配置数据（用于保存）
    private static Config currentConfig;

    /**
     * 内部配置数据结构。
     */
    private static class Config {
        List<String> canCrushed;
        List<String> haveExtraDrop;
        List<String> blacklist;
    }

    /**
     * 加载配置文件，若文件不存在则创建默认配置。
     *
     * @return 配置解析后的方块集合（包含 canCrushed 和 haveExtraDrop）
     */
    public static Map<String, Set<Block>> load() {
        try {
            // 确保配置目录存在
            if (Files.notExists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            // 如果配置文件不存在，写入默认配置
            if (Files.notExists(CONFIG_FILE)) {
                writeDefaultConfig();
            }

            // 读取并解析配置
            String json = Files.readString(CONFIG_FILE);
            currentConfig = GSON.fromJson(json, Config.class);

            // 构建最终的方块集合
            Set<Block> canCrushed = parseBlockList(currentConfig.canCrushed, DEFAULT_CAN_CRUSHED);
            Set<Block> haveExtra = parseBlockList(currentConfig.haveExtraDrop, DEFAULT_HAVE_EXTRA);

            // 应用黑名单
            if (currentConfig.blacklist != null) {
                Set<Block> blacklistBlocks = parseBlockList(currentConfig.blacklist, Collections.emptyList());
                canCrushed.removeAll(blacklistBlocks);
                haveExtra.removeAll(blacklistBlocks);
            }

            Map<String, Set<Block>> result = new HashMap<>();
            result.put("canCrushed", canCrushed);
            result.put("haveExtra", haveExtra);
            return result;

        } catch (IOException | JsonParseException e) {
            LOGGER.error("Fails to load the profile and the default list is used", e);
            // 加载失败时返回默认列表（基于硬编码）
            return getDefaultSets();
        }
    }

    /**
     * 保存当前配置到文件。
     *
     * @param canCrushedBlocks 当前可破碎方块集合
     * @param haveExtraBlocks  当前额外掉落方块集合
     */
    public static void save(Set<Block> canCrushedBlocks, Set<Block> haveExtraBlocks) {
        // 将 Block 集合转换为 ID 列表
        List<String> canCrushedIds = canCrushedBlocks.stream()
                .map(block -> Registries.BLOCK.getId(block).toString())
                .sorted()
                .collect(Collectors.toList());

        List<String> haveExtraIds = haveExtraBlocks.stream()
                .map(block -> Registries.BLOCK.getId(block).toString())
                .sorted()
                .collect(Collectors.toList());

        // 构建配置对象（黑名单保持为空或保留原有？这里简单清空黑名单，因为保存时是完整列表）
        Config config = new Config();
        config.canCrushed = canCrushedIds;
        config.haveExtraDrop = haveExtraIds;
        config.blacklist = Collections.emptyList(); // 保存时不使用黑名单

        try {
            if (Files.notExists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_FILE, json);
            currentConfig = config; // 更新内存中的配置
        } catch (IOException e) {
            LOGGER.error("Saving the profile failed", e);
        }
    }

    /**
     * 写入默认配置文件。
     */
    private static void writeDefaultConfig() throws IOException {
        Config defaultConfig = new Config();
        defaultConfig.canCrushed = DEFAULT_CAN_CRUSHED;
        defaultConfig.haveExtraDrop = DEFAULT_HAVE_EXTRA;
        defaultConfig.blacklist = Collections.emptyList();
        String json = GSON.toJson(defaultConfig);
        Files.writeString(CONFIG_FILE, json);
    }

    /**
     * 解析方块 ID 列表，返回有效的 Block 集合。
     */
    private static Set<Block> parseBlockList(List<String> ids, List<String> fallbackIds) {
        List<String> source = (ids != null && !ids.isEmpty()) ? ids : fallbackIds;
        Set<Block> blocks = new HashSet<>();
        for (String id : source) {
            Block block = Registries.BLOCK.get(Identifier.tryParse(id));
            if (block != Blocks.AIR) {
                blocks.add(block);
            } else {
                LOGGER.warn("Unknown block ID: {}, ignored", id);
            }
        }
        return blocks;
    }

    /**
     * 获取基于硬编码默认值的方块集合（当配置加载失败时使用）。
     */
    private static Map<String, Set<Block>> getDefaultSets() {
        Set<Block> canCrushed = parseBlockList(DEFAULT_CAN_CRUSHED, Collections.emptyList());
        Set<Block> haveExtra = parseBlockList(DEFAULT_HAVE_EXTRA, Collections.emptyList());
        Map<String, Set<Block>> result = new HashMap<>();
        result.put("canCrushed", canCrushed);
        result.put("haveExtra", haveExtra);
        return result;
    }
}