package org.xs4j;

import javax.xml.stream.XMLOutputFactory;

/**
 * Created by mturski on 12/8/2016.
 */
public class XMLSpitterFactory {
    private static XMLSpitterFactory instance;

    public static XMLSpitterFactory getInstance() {
        if (instance == null)
            instance = new XMLSpitterFactory();

        return instance;
    }

    private XMLSpitterFactory() {
    }

    public XMLSpitter createXMLSpitter() {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        return new StAXSpitter(xmlOutputFactory);
    }
}
