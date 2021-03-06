package org.xs4j.xmlspitter;

/**
 * Created by mturski on 12/8/2016.
 */
public interface XMLSpitterFactory {
    String DEFAULT_XML_DOCUMENT_VERSION = "1.0";
    String DEFAULT_XML_DOCUMENT_ENCODING = "UTF-8";

    /**
     * Returns a new instance of {@link XMLSpitter}
     *
     * @return a new instance of <code>XMLSpitter</code>
     */
    XMLSpitter createXMLSpitter();
}
