package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;

import java.util.List;

/**
 * Created by mturski on 11/13/2016.
 */
abstract class NodePath {
    abstract boolean checkAlignment(@NotNull XMLNode node, int depthLevel);

    abstract List<String> getPath();
}
