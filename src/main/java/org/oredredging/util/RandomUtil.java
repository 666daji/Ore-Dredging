package org.oredredging.util;

import java.util.Random;

public class RandomUtil {
    private static final Random RANDOM = new Random();

    /**
     * 根据指定的概率随机返回 true 或 false。
     *
     * @param probability 返回 true 的概率，取值范围 [0.0, 1.0]
     * @return 以 probability 概率返回 true，否则返回 false
     * @throws IllegalArgumentException 如果 probability 不在 [0,1] 范围内
     */
    public static boolean randomBoolean(float probability) {
        if (probability < 0.0f || probability > 1.0f) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }

        return RANDOM.nextFloat() < probability;
    }
}
