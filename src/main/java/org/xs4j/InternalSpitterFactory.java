package org.xs4j;

import org.xs4j.XMLSpitterImpl.InternalStreamProvider;

/**
 * Created by mturski on 1/5/2017.
 */
public class InternalSpitterFactory implements XMLSpitterFactory {
    public static XMLSpitterFactory getInstance() {
        return new InternalSpitterFactory();
    }

    private InternalSpitterFactory() {
    }

    @Override
    public XMLSpitter createXMLSpitter() {
        return new XMLSpitterImpl(new InternalStreamProvider());
    }
}
