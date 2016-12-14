package org.xs4j;

import javax.xml.XMLConstants;
import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public final class XMLNodeFactory {
    static final String QNAME_SEPARATOR = ":";
    static final String XMLNS_WITH_SEPARATOR = XMLConstants.XMLNS_ATTRIBUTE + QNAME_SEPARATOR;

    public static XMLNodeFactory getInstance() {
        return new XMLNodeFactory();
    }

    private XMLNodeFactory() {
    }


    public XMLNode createNode(long id, String localName, Map<String, String> attributeByName) {
        return new XMLNodeImpl(id, null, null, localName, attributeByName);
    }

    public XMLNode createNode(long id, String namespace, String prefix, String localName, Map<String, String> attributeByName) {
        return new XMLNodeImpl(id, namespace, prefix, localName, attributeByName);
    }

    static void appendText(XMLNode node, char[] ch, int start, int length) {
        ((XMLNodeImpl)node).appendText(ch, start, length);
    }
}
