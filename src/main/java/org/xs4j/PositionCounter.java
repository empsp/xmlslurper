package org.xs4j;

import java.util.Arrays;

/**
 * Created by mturski on 12/22/2016.
 */
class PositionCounter {
    public static final int DEFAULT_SIZE = 4;

    private long[] posByDepth = new long[DEFAULT_SIZE];
    private int prevDepth;

    PositionCounter() {
    }

    long getNodePosition(int depth) {
        if (depth > prevDepth)
            growWhenMaxCapacity(depth);
        else if (depth < prevDepth)
            posByDepth[depth] = 0L;

        posByDepth[depth - 1]++;
        prevDepth = depth;

        return posByDepth[depth - 1];
    }

    private void growWhenMaxCapacity(int depth) {
        if (depth >= posByDepth.length)
            posByDepth = Arrays.copyOf(posByDepth, posByDepth.length << 1);
    }

    void reset() {
        posByDepth = new long[DEFAULT_SIZE];
        prevDepth = 0;
    }
}
