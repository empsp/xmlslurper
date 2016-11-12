package com.tsolutions.xmlslurper.util;

import java.util.Map;

/**
 * Created by mturski on 11/4/2016.
 */
public final class NotNullValidator {
    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();

        return obj;
    }

    public static <K, V> Map<K, V> requireNonNull(Map<K, V> map) {
        if (map == null)
            throw new NullPointerException();

        for(Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getKey() == null)
                throw new NullPointerException();

            if (entry.getValue() == null)
                throw new NullPointerException();
        }

        return map;
    }
}
