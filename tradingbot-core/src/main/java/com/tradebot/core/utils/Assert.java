package com.tradebot.core.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Assert {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
