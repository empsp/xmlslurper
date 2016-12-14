package org.xs4j;

import javax.xml.stream.XMLOutputFactory;

/**
 * Created by mturski on 12/8/2016.
 */
public class XMLSpitterFactory {
    public static XMLSpitterFactory getInstance() {
        return new XMLSpitterFactory();
    }

    private XMLSpitterFactory() {
    }

    public XMLSpitter createXMLSpitter() {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        return new StAXSpitter(xmlOutputFactory);
    }
}
