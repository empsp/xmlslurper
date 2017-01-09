package org.xs4j.xmlspitter;

import org.xs4j.XMLNode;
import org.xs4j.XMLNodeFactory;
import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.Map;

import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 12/8/2016.
 */
public class StAXStream implements XMLStream {
    private final long id;
    private final XMLStreamWriter stream;

    private int defaultNamespaceAtDepth = Integer.MAX_VALUE;

    StAXStream(long id, XMLStreamWriter stream) {
        this.id = id;
        this.stream = stream;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void writeElement(@NotNull XMLNode node) {
        writeStartElement(node);

        String characters = node.getText();
        if (characters != null && !characters.isEmpty())
            writeCharacters(characters);

        writeEndElement();
    }

    @Override
    public void writeStartDocument() {
        doWriteStartDocument(XMLSpitterFactory.DEFAULT_XML_DOCUMENT_ENCODING, XMLSpitterFactory.DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void writeStartDocument(String encoding) {
        doWriteStartDocument(encoding, XMLSpitterFactory.DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void writeStartDocument(String encoding, String version) {
        doWriteStartDocument(encoding, version);
    }

    public void doWriteStartDocument(String encoding, String version) {
        try {
            stream.writeStartDocument(encoding, version);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void writeStartElement(@NotNull XMLNode node) {
        try {
            doWriteStartElement(node);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    private void doWriteStartElement(XMLNode node) throws XMLStreamException {
        requireNonNull(node);

        writeStartElementWithNamespaces(node);
        writeAttributesAndNamespaces(node);
    }

    private void writeStartElementWithNamespaces(XMLNode node) throws XMLStreamException {
        String prefix = node.getPrefix();
        String namespace = node.getNamespace();

        if (namespace != null) {
            if (prefix != null) { // handle element names eg. prefix:name
                stream.writeStartElement(prefix, node.getLocalName(), namespace);
            } else { // handle element names eg. name with default namespace
                stream.writeStartElement(node.getLocalName());
            }
        } else // handle element names eg. name without default namespace
            stream.writeStartElement(node.getLocalName());
    }

    private void writeAttributesAndNamespaces(XMLNode node) throws XMLStreamException {
        Map<String, String> attributeByQName = node.getAttributes();

        if (node.getDepth() <= defaultNamespaceAtDepth) {
            if (node.getPrefix() == null && node.getNamespace() != null) {
                defaultNamespaceAtDepth = node.getDepth();

                stream.writeDefaultNamespace(node.getNamespace());
            } else
                defaultNamespaceAtDepth = Integer.MAX_VALUE;
        }

        Map<String, String> namespaceURIByPrefix = new HashMap<String, String>();
        for (String qName : attributeByQName.keySet()) {
            if (qName.startsWith(XMLNodeFactory.XMLNS_WITH_SEPARATOR)) {
                String prefix = qName.substring(XMLNodeFactory.XMLNS_WITH_SEPARATOR.length());
                String namespaceURI = attributeByQName.get(qName);

                namespaceURIByPrefix.put(prefix, namespaceURI);
                stream.writeNamespace(prefix, namespaceURI);
            }
        }

        for (String qName : attributeByQName.keySet()) {
            if (!qName.startsWith(XMLNodeFactory.XMLNS_WITH_SEPARATOR)) {
                String attribute = attributeByQName.get(qName);
                int separatorIndex = qName.indexOf(XMLNodeFactory.QNAME_SEPARATOR);

                if (separatorIndex > 0) {
                    String prefix = qName.substring(0, separatorIndex);
                    String localName = qName.substring(separatorIndex + 1);
                    String namespaceURI = namespaceURIByPrefix.get(prefix);

                    stream.writeAttribute(prefix, namespaceURI, localName, attribute);
                } else
                    stream.writeAttribute(qName, attribute);
            }
        }
    }

    @Override
    public void writeCharacters(@Nullable String characters) {
        try {
            stream.writeCharacters(characters);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void writeCharacters(@NotNull char[] characters) {
        writeCharacters(characters, 0, characters.length);
    }

    @Override
    public void writeCharacters(@NotNull char[] characters, int startPos, int length) {
        try {
            stream.writeCharacters(characters, startPos, length);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void writeCharacters(@NotNull XMLNode node) {
        requireNonNull(node);

        String text = node.getText();
        if (text != null)
            writeCharacters(text);
    }

    @Override
    public void writeEndElement() {
        try {
            stream.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    /**
     * Due to how {@link XMLStreamWriter} keeps start/end-tag stack (and throws Exceptions on inconsistencies) it's
     * impossible to support this function.
     *
     * @param node to match the end-tag
     */
    @Override
    public void writeEndElement(@NotNull XMLNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
        try {
            stream.flush();
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            stream.close();
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
