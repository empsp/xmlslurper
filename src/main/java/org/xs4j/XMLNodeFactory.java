package org.xs4j;

import javax.xml.XMLConstants;
import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public final class XMLNodeFactory {
    public static final String QNAME_SEPARATOR = ":";
    public static final String XMLNS_WITH_SEPARATOR = XMLConstants.XMLNS_ATTRIBUTE + QNAME_SEPARATOR;

    public static XMLNodeFactory getInstance() {
        return new XMLNodeFactory();
    }

    private XMLNodeFactory() {
    }


    public final XMLNode createNode(long id, String localName, Map<String, String> attributeByName) {
        return new XMLNodeImpl(id, null, null, localName, attributeByName);
    }

    public final XMLNode createNode(long id, String namespace, String prefix, String localName, Map<String, String> attributeByName) {
        return new XMLNodeImpl(id, namespace, prefix, localName, attributeByName);
    }
}
