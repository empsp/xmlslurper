package org.xs4j;

import org.xml.sax.Attributes;

/**
 * Created by mturski on 11/29/2016.
 */
abstract class NamespaceSensitiveElementParser {
    XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
        throw new UnsupportedOperationException();
    }
}
