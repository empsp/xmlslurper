package org.xs4j;

import javax.xml.XMLConstants;
import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
abstract class NodeFactory {
    static final String QNAME_SEPARATOR = ":";
    static final String XMLNS_WITH_SEPARATOR = XMLConstants.XMLNS_ATTRIBUTE + QNAME_SEPARATOR;

    abstract XMLNode createNode(long id, String localName, Map<String, String> attributeByName);

    abstract XMLNode createNode(long id, String namespace, String prefix, String localName, Map<String, String> attributeByName);
}
