package org.xs4j;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Created by mturski on 12/8/2016.
 */
public class StAXStream implements XMLStream {
    private final long id;
    private final XMLStreamWriter writer;

    private Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

    StAXStream(long id, XMLStreamWriter writer) {
        this.id = id;
        this.writer = writer;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void writeElement(XMLNode node) {
        writeStartElement(node);
        writeCharacters(node.getText());
        writeEndElement();
    }

    @Override
    public void writeStartElement(XMLNode node) {
        try {
            doWriteStartElement(node);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    private void doWriteStartElement(XMLNode node) throws XMLStreamException {
        descendants.addLast(node);

        if (node.getNamespace() != null) {
            if (node.getPrefix() != null)
                writer.writeStartElement(node.getPrefix(), node.getLocalName(), node.getNamespace());
            else
                writer.writeStartElement(node.getNamespace(), node.getLocalName());
        } else
            writer.writeStartElement(node.getLocalName());

        for (Map.Entry<String, String> entry : node.getAttributes().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            int separatorIndex = name.indexOf(XMLNodeFactory.QNAME_SEPARATOR);

            if (name.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                if (separatorIndex > 0)
                    writer.writeNamespace(name.substring(separatorIndex), value);
                else
                    writer.writeDefaultNamespace(value);
            } else if (separatorIndex > 0) {
                writer.writeAttribute(name.substring(0, separatorIndex), "", name.substring(separatorIndex), value);
            } else
                writer.writeAttribute(name, value);
        }
    }

    @Override
    public void writeCharacters(String characters) {
        try {
            writer.writeCharacters(characters);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void writeEndElement() {
        try {
            writer.writeEndElement();

            descendants.removeLast();
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void writeEndElement(XMLNode node) {
        try {
            while (!node.equals(descendants.peekLast())) {
                writer.writeEndElement();

                descendants.removeLast();
            }
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XMLStream)) return false;

        XMLStream that = (XMLStream) o;

        return id == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
