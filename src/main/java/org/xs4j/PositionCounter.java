package org.xs4j;

import org.xs4j.util.ArraysUtil;

import java.util.Arrays;

/**
 * Created by mturski on 12/22/2016.
 */
class PositionCounter {
    private static final int DEFAULT_SIZE = 4;

    private long[] posByDepth = new long[DEFAULT_SIZE];
    private int prevDepth;

    PositionCounter() {
    }

    long getNodePosition(int depth) {
        if (depth >= posByDepth.length)
            posByDepth = Arrays.copyOf(posByDepth, ArraysUtil.safelyDoubleLengthValue(posByDepth.length));
        else if (depth < prevDepth)
            posByDepth[prevDepth - 1] = 0L;

        prevDepth = depth;
        return ++posByDepth[depth - 1];
    }

    void reset() {
        posByDepth = new long[DEFAULT_SIZE];
        prevDepth = 0;
    }
}
