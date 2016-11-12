package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.path.Node;

import javax.xml.stream.XMLInputFactory;
import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public class StAXSlurperFactory {
    private static StAXSlurperFactory instance;

    public static StAXSlurperFactory getInstance() {
        if (instance == null)
            instance = new StAXSlurperFactory();

        return instance;
    }

    private StAXSlurperFactory() {
    }

    public XMLSlurper createXMLSlurper() {
        return new StAXSlurper(XMLInputFactory.newInstance(), getStAXNodeFactory());
    }

    static NodeFactory getStAXNodeFactory() {
        return new NodeFactory() {
            @Override
            public Node createNode(long id, String name, Map<String, String> attributeByName) {
                return new StAXNode(id, name, attributeByName);
            }
        };
    }
}
