package org.xs4j;

import org.xs4j.XMLSpitterImpl.StAXStreamProvider;

import javax.xml.stream.XMLOutputFactory;

/**
 * Created by mturski on 1/5/2017.
 */
public class StAXSpitterFactory implements XMLSpitterFactory {
    public static XMLSpitterFactory getInstance() {
        return new StAXSpitterFactory();
    }

    private StAXSpitterFactory() {
    }

    @Override
    public XMLSpitter createXMLSpitter() {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        return new XMLSpitterImpl(new StAXStreamProvider(xmlOutputFactory));
    }
}
