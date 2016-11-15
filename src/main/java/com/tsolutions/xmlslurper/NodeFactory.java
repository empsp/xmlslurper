package com.tsolutions.xmlslurper;

import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
abstract class NodeFactory {
    abstract XMLNode createNode(long id, String name, Map<String, String> attributeByName);

    abstract XMLNode createNode(long id, String name, String[][] attributeByName);
}
