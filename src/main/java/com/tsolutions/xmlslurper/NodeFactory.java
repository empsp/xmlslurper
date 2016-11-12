package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.path.Node;

import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public interface NodeFactory {
    Node createNode(long id, String name, Map<String, String> attributeByName);
}
