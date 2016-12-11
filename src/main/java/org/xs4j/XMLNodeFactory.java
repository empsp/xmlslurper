package org.xs4j;

import javax.xml.XMLConstants;
import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public class XMLNodeFactory {
    static final String QNAME_SEPARATOR = ":";
    static final String XMLNS_WITH_SEPARATOR = XMLConstants.XMLNS_ATTRIBUTE + QNAME_SEPARATOR;

    private static XMLNodeFactory instance;

    public static XMLNodeFactory getInstance() {
        if (instance == null)
            instance = new XMLNodeFactory();

        return instance;
    }

    private XMLNodeFactory() {
    }


    XMLNode createNode(long id, String localName, Map<String, String> attributeByName) {
        return new XMLNodeImpl(id, null, null, localName, attributeByName);
    }

    XMLNode createNode(long id, String namespace, String prefix, String localName, Map<String, String> attributeByName) {
        return new XMLNodeImpl(id, namespace, prefix, localName, attributeByName);
    }
}
