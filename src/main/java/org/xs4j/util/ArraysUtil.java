package org.xs4j.util;

/**
 * Created by mturski on 1/8/2017.
 */
public final class ArraysUtil {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; // follows MAX_ARRAY_SIZE from java.util.ArrayList
    private static final int HALF_MAX_ARRAY_SIZE = MAX_ARRAY_SIZE >> 1;

    public static final int safelyDoubleLengthValue(int length) {
        return length < HALF_MAX_ARRAY_SIZE ? length << 1 : MAX_ARRAY_SIZE;
    }
}
