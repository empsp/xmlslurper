package org.xs4j;

/**
 * Created by mturski on 12/8/2016.
 */
public interface XMLStream {
    long getId();

    void writeElement(XMLNode node);

    void writeStartElement(XMLNode node);

    void writeCharacters(String characters);

    void writeEndElement();

    void writeEndElement(XMLNode node);

    void flush();

    void close();
}
