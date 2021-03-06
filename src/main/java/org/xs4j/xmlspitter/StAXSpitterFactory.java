package org.xs4j.xmlspitter;

import javax.xml.stream.XMLOutputFactory;

/**
 * Created by mturski on 1/5/2017.
 */
public final class StAXSpitterFactory implements XMLSpitterFactory {
    public static XMLSpitterFactory getInstance() {
        return new StAXSpitterFactory();
    }

    private StAXSpitterFactory() {
    }

    @Override
    public final XMLSpitter createXMLSpitter() {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        return new XMLSpitterImpl(new XMLSpitterImpl.StAXStreamProvider(xmlOutputFactory));
    }
}
