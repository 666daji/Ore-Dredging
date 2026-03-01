package org.oredredging.config.framework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.oredredging.OreDredging;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 配置管理器，负责所有配置的注册、加载、保存和访问。
 * <p>所有配置文件存放在 {@code config/tw_ore_dredging/} 目录下。</p>
 * 配置文件为 JSON 格式，顶层必须是一个对象，并包含一个 "version" 字段（由框架自动管理）。
 */
public final class ConfigManager {
    // 基础配置目录
    private static final Path BASE_DIR = FabricLoader.getInstance().getConfigDir().resolve("tw_ore_dredging");
    // 当前全局配置版本号，所有配置共用此版本
    private static final int CURRENT_VERSION = 1;

    // 类型到数据的缓存
    private static final Map<ConfigType<?>, Object> CACHE = new LinkedHashMap<>();
    // 名称到类型的映射，用于按名称查找
    private static final Map<String, ConfigType<?>> NAME_TO_TYPE = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = OreDredging.LOGGER;

    private ConfigManager() {}

    /**
     * 注册一个配置类型。
     * @param type 配置类型
     * @throws IllegalArgumentException 如果名称已被注册
     */
    public static void register(ConfigType<?> type) {
        if (NAME_TO_TYPE.containsKey(type.name())) {
            throw new IllegalArgumentException("Duplicate config type name: " + type.name());
        }
        NAME_TO_TYPE.put(type.name(), type);
    }

    /**
     * 加载所有已注册的配置。
     * 应在注册完成后调用（通常在模组初始化阶段）。
     */
    public static void loadAll() {
        NAME_TO_TYPE.values().forEach(ConfigManager::load);
    }

    /**
     * 根据配置类型获取数据。
     * @param type 配置类型
     * @param <T>  数据类型
     * @return 配置数据，如果未加载则返回 null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T get(ConfigType<T> type) {
        return (T) CACHE.get(type);
    }

    /**
     * 根据名称获取配置数据（类型不安全，推荐仅在动态场景使用）。
     * @param name 配置名称
     * @return 配置数据，如果不存在或未加载则返回 null
     */
    @Nullable
    public static Object get(String name) {
        ConfigType<?> type = NAME_TO_TYPE.get(name);
        return type != null ? CACHE.get(type) : null;
    }

    /**
     * 更新一个配置，修改后自动保存到文件。
     * @param type    配置类型
     * @param updater 更新函数，接收旧数据返回新数据
     * @param <T>     数据类型
     * @throws IllegalStateException 如果指定类型的配置尚未加载
     */
    public static <T> void update(ConfigType<T> type, Function<T, T> updater) {
        T old = get(type);
        if (old == null) {
            throw new IllegalStateException("Configuration not loaded: " + type.name());
        }
        T newData = updater.apply(old);
        CACHE.put(type, newData);
        save(type, newData);
    }

    /**
     * 加载单个配置类型。
     */
    private static <T> void load(ConfigType<T> type) {
        Path file = BASE_DIR.resolve(type.name() + ".json");
        T data = type.defaultValue();
        boolean shouldSave = false; // 标记是否需要写入文件

        if (Files.exists(file)) {
            Optional<T> loaded = loadFromFile(file, type);
            if (loaded.isPresent()) {
                data = loaded.get();
            } else {
                // 文件存在但加载失败（语法错误、字段缺失等），使用默认值并标记需要覆盖
                LOGGER.warn("Using default configuration for '{}' due to loading failure (invalid or corrupted file)", type.name());
                shouldSave = true;
            }
        } else {
            LOGGER.info("Configuration file '{}' does not exist, will create with default values", file);
            shouldSave = true; // 文件不存在，需要创建
        }

        // 存入缓存
        CACHE.put(type, data);

        // 如果需要保存（文件缺失、损坏）或数据已变更（例如迁移后），则写入文件
        if (shouldSave || data != type.defaultValue()) {
            save(type, data);
        }
    }

    /**
     * 从文件加载配置，处理版本检查和迁移。
     */
    private static <T> Optional<T> loadFromFile(Path file, ConfigType<T> type) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            JsonElement json = JsonParser.parseString(content);

            if (!json.isJsonObject()) {
                LOGGER.error("Configuration file '{}' is not a JSON object", file);
                return Optional.empty();
            }

            JsonObject obj = json.getAsJsonObject();
            int fileVersion = obj.has("version") ? obj.get("version").getAsInt() : 0;

            // 根据版本处理
            return handleVersion(obj, fileVersion, type);

        } catch (IOException e) {
            LOGGER.error("Failed to read configuration file '{}': {}", file, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            // 捕获 JsonParseException 等其他异常
            LOGGER.error("Failed to parse configuration file '{}': {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 根据文件中的版本号处理加载逻辑：版本匹配直接解析，版本过低则尝试迁移，版本过高则视为无效。
     */
    private static <T> Optional<T> handleVersion(JsonObject obj, int fileVersion, ConfigType<T> type) {
        if (fileVersion == CURRENT_VERSION) {
            // 版本匹配，直接解析
            return parseWithCodec(obj, type);
        } else if (fileVersion < CURRENT_VERSION) {
            // 版本过低，需要迁移
            if (type.migrator() != null) {
                LOGGER.info("Migrating configuration '{}' from version {} to {}", type.name(), fileVersion, CURRENT_VERSION);
                DataResult<T> result = type.migrator().migrate(obj, fileVersion);
                Optional<T> migrated = result.resultOrPartial(error ->
                        LOGGER.error("Migration failed for '{}': {}", type.name(), error));
                if (migrated.isPresent()) {
                    return migrated;
                } else {
                    LOGGER.warn("Migration failed for '{}', using default", type.name());
                    return Optional.empty();
                }
            } else {
                LOGGER.warn("Configuration '{}' version {} is older than current {}, but no migrator provided. Using default.",
                        type.name(), fileVersion, CURRENT_VERSION);
                return Optional.empty();
            }
        } else {
            // 版本过高
            LOGGER.warn("Configuration '{}' version {} is newer than current {}. Using default.",
                    type.name(), fileVersion, CURRENT_VERSION);
            return Optional.empty();
        }
    }

    /**
     * 使用配置的 Codec 解析 JSON 对象。
     */
    private static <T> Optional<T> parseWithCodec(JsonObject obj, ConfigType<T> type) {
        JsonObject dataObj = obj.deepCopy();
        dataObj.remove("version");

        DataResult<T> result = type.codec().parse(JsonOps.INSTANCE, dataObj);
        return result.resultOrPartial(error ->
                LOGGER.error("Failed to parse configuration '{}': {}", type.name(), error));
    }

    /**
     * 保存指定类型的配置数据到文件。
     */
    private static <T> void save(ConfigType<T> type, T data) {
        Path file = BASE_DIR.resolve(type.name() + ".json");
        try {
            Files.createDirectories(file.getParent());

            // 将数据对象编码为 JsonElement
            JsonElement json = type.codec().encodeStart(JsonOps.INSTANCE, data)
                    .getOrThrow(false, s -> LOGGER.error("Failed to encode configuration '{}': {}", type.name(), s));

            // 确保顶层是 JsonObject，并添加版本号
            JsonObject obj = ensureJsonObject(json);
            obj.addProperty("version", CURRENT_VERSION);

            String content = GSON.toJson(obj);
            Files.writeString(file, content, StandardCharsets.UTF_8);
            LOGGER.debug("Configuration '{}' saved successfully", type.name());

        } catch (Exception e) {
            LOGGER.error("Failed to save configuration '{}'", type.name(), e);
        }
    }

    /**
     * 确保给定的 JsonElement 是一个 JsonObject，如果不是则将其包装在 {"value": ...} 中。
     */
    private static JsonObject ensureJsonObject(JsonElement json) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject();
        } else {
            JsonObject wrapper = new JsonObject();
            wrapper.add("value", json);
            return wrapper;
        }
    }
}