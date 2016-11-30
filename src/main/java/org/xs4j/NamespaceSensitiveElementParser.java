package org.xs4j;

import org.xml.sax.Attributes;

import javax.xml.stream.XMLStreamReader;

/**
 * Created by mturski on 11/29/2016.
 */
abstract class NamespaceSensitiveElementParser {
    NodeFactory nodeFactory;

    NamespaceSensitiveElementParser(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    XMLNode parseStartElement(XMLStreamReader parser) {
        throw new UnsupportedOperationException();
    }

    XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
        throw new UnsupportedOperationException();
    }
}
