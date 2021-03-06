package org.xs4j.util;

import java.util.Map;

/**
 * Created by mturski on 11/4/2016.
 */
public final class NonNullValidator {
    public static final <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();

        return obj;
    }

    public static final <T> T[] requireNonNull(T[] objs) {
        if (objs == null)
            throw new NullPointerException();

        for (T obj : objs)
            if (obj == null)
                throw new NullPointerException();

        return objs;
    }

    public static final <K, V> Map<K, V> requireNonNull(Map<K, V> map) {
        if (map == null)
            throw new NullPointerException();

        for (K key : map.keySet()) {
            if (key == null)
                throw new NullPointerException();

            if (map.get(key) == null)
                throw new NullPointerException();
        }

        return map;
    }
}
