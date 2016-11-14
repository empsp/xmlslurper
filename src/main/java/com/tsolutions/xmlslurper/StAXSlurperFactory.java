package com.tsolutions.xmlslurper;

import javax.xml.stream.XMLInputFactory;
import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public final class StAXSlurperFactory {
    private static StAXSlurperFactory instance;

    public static StAXSlurperFactory getInstance() {
        if (instance == null)
            instance = new StAXSlurperFactory();

        return instance;
    }

    private StAXSlurperFactory() {
    }

    public XMLSlurper createXMLSlurper() {
        return new StAXSlurper(XMLInputFactory.newInstance(), getStAXNodeFactory(), getNodePathFactory());
    }

    static NodeFactory getStAXNodeFactory() {
        return new NodeFactory() {
            @Override
            XMLNode createNode(long id, String name, Map<String, String> attributeByName) {
                return new StAXNode(id, name, attributeByName);
            }
        };
    }

    public NodePathFactory getNodePathFactory() {
        return new NodePathFactory();
    }
}
