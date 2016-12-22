package org.xs4j;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by mturski on 12/22/2016.
 */
class PositionCounter {
    private Deque<Long> posByDepth = new ArrayDeque<Long>();
    private int prevDepth;

    PositionCounter() {
    }

    long getNodePosition(int depth) {
        if (depth > prevDepth)
            posByDepth.addLast(1L);
        else if (depth == prevDepth)
            posByDepth.addLast(posByDepth.removeLast() + 1);
        else {
            posByDepth.removeLast();

            if (posByDepth.size() > 0)
                posByDepth.addLast(posByDepth.removeLast() + 1);
        }

        prevDepth = depth;
        return posByDepth.peekLast();
    }

    void reset() {
        posByDepth.clear();
        prevDepth = 0;
    }
}
