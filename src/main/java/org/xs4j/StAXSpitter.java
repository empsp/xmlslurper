package org.xs4j;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

/**
 * Created by mturski on 12/8/2016.
 */
public class StAXSpitter implements XMLSpitter {
    public static final String DEFAULT_XML_DOCUMENT_VERSION = "1.0";
    public static final String DEFAULT_XML_DOCUMENT_ENCODING = "UTF-8";
    private static long idFeed;

    private final XMLOutputFactory xmlOutputFactory;

    StAXSpitter(XMLOutputFactory xmlOutputFactory) {
        idFeed = 0L;

        this.xmlOutputFactory = xmlOutputFactory;
    }

    @Override
    public XMLStream write(OutputStream outputStream) {
        try {
            return doWrite(outputStream, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream write(OutputStream outputStream, String version) {
        try {
            return doWrite(outputStream, DEFAULT_XML_DOCUMENT_ENCODING, version);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream write(OutputStream outputStream, String version, String encoding) {
        try {
            return doWrite(outputStream, encoding, version);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    private XMLStream doWrite(OutputStream outputStream, String encoding, String version) throws XMLStreamException {
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);
        writer.writeStartDocument(encoding, version);

        return new StAXStream(idFeed++, writer);
    }
}
