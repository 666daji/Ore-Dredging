package org.oredredging.config.framework;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

/**
 * 配置类型，代表一种配置的元信息。
 * 每个配置类型实例全局唯一，通过名称标识。
 *
 * @param name         配置名称（也用作文件名，不含 .json 后缀）
 * @param codec        当前版本的 Codec
 * @param defaultValue 默认配置数据
 * @param migrator     迁移器（可为 null，表示不支持自动迁移）
 * @param <T>          配置数据类型
 */
public record ConfigType<T>(
        String name,
        Codec<T> codec,
        T defaultValue,
        @Nullable ConfigMigrator<T> migrator
) {
    /**
     * 静态工厂方法，简化创建。
     */
    public static <T> ConfigType<T> of(String name, Codec<T> codec, T defaultValue, @Nullable ConfigMigrator<T> migrator) {
        return new ConfigType<>(name, codec, defaultValue, migrator);
    }
}