package org.xs4j.xmlspitter;

/**
 * Created by mturski on 1/5/2017.
 */
public final class InternalSpitterFactory implements XMLSpitterFactory {
    public static XMLSpitterFactory getInstance() {
        return new InternalSpitterFactory();
    }

    private InternalSpitterFactory() {
    }

    @Override
    public final XMLSpitter createXMLSpitter() {
        return new XMLSpitterImpl(new XMLSpitterImpl.InternalStreamProvider());
    }
}
