package com.tsolutions.xmlslurper;

import java.util.Deque;
import java.util.List;

/**
 * Created by mturski on 11/13/2016.
 */
abstract class SlurpAlignment {
    abstract boolean checkAlignment(int depth, XMLNode lastNode);

    abstract boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode);

    abstract List<String> getPath();
}
