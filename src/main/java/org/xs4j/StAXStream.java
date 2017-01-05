package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashMap;
import java.util.Map;

import static org.xs4j.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_ENCODING;
import static org.xs4j.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_VERSION;
import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 12/8/2016.
 */
public class StAXStream implements XMLStream {
    private static final String EMPTY_PREFIX = "";

    private final long id;
    private final XMLStreamWriter stream;

    private Map<String, String> namespaceByPrefix = new HashMap<String, String>();

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
        doWriteStartDocument(DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void writeStartDocument(String encoding) {
        doWriteStartDocument(encoding, DEFAULT_XML_DOCUMENT_VERSION);
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
        writeAttributesWithNamespaces(node);
    }

    private void writeStartElementWithNamespaces(XMLNode node) throws XMLStreamException {
        String prefix = node.getPrefix();
        String namespace = node.getNamespace();

        if (namespace != null) {
            if (prefix != null) { // handle element names eg. prefix:name
                namespaceByPrefix.put(prefix, namespace);

                stream.writeStartElement(prefix, node.getLocalName(), namespace);
            } else { // handle element names eg. name with default namespace
                if (!namespaceByPrefix.containsKey(EMPTY_PREFIX)) {
                    namespaceByPrefix.put(EMPTY_PREFIX, namespace);

                    stream.writeStartElement(node.getLocalName());
                    stream.writeDefaultNamespace(namespace);
                } else {
                    stream.writeStartElement(EMPTY_PREFIX, node.getLocalName(), namespace);
                }
            }
        } else // handle element names eg. name without default namespace
            stream.writeStartElement(node.getLocalName());
    }

    private void writeAttributesWithNamespaces(XMLNode node) throws XMLStreamException {
        String prefix;
        String namespace;
        String name;
        String value;
        int separatorIndex;

        for (Map.Entry<String, String> entry : node.getAttributes().entrySet()) {
            name = entry.getKey();
            value = entry.getValue();
            separatorIndex = name.indexOf(XMLNodeFactory.QNAME_SEPARATOR);

            if (separatorIndex > 0) {
                if (name.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) { // handle attribute names eg. xmlns:prefix
                    prefix = name.substring(separatorIndex + 1);

                    if(!namespaceByPrefix.containsKey(prefix)) { // if new
                        namespaceByPrefix.put(prefix, value);

                        stream.writeNamespace(prefix, value);
                    }
                } else { // handle attribute names eg. prefix:name
                    prefix = name.substring(0, separatorIndex);

                    if(!namespaceByPrefix.containsKey(prefix)) { // if new
                        namespace = findNamespaceByPrefix(prefix, node.getAttributes()); // the namespace is yet to be extracted from attributes
                        namespaceByPrefix.put(prefix, namespace);

                        stream.writeNamespace(prefix, namespace);
                    } else
                        namespace = namespaceByPrefix.get(prefix);

                    stream.writeAttribute(prefix, namespace, name.substring(separatorIndex + 1), value);
                }
            } else if (name.startsWith(XMLConstants.XMLNS_ATTRIBUTE) && !namespaceByPrefix.containsKey(EMPTY_PREFIX)) { // handle attribute names eg. xlmns, if new
                namespaceByPrefix.put(EMPTY_PREFIX, value);

                stream.writeDefaultNamespace(value);
            } else // handle attribute names eg. name
                stream.writeAttribute(name, value);
        }
    }

    private String findNamespaceByPrefix(String prefix, Map<String, String> attributes) {
        int separatorIndex;
        for (String name : attributes.keySet()) {
            separatorIndex = name.indexOf(XMLNodeFactory.QNAME_SEPARATOR);

            if (separatorIndex > 0 && prefix.equals(name.substring(separatorIndex + 1)))
                return attributes.get(name);
        }

        throw new IllegalStateException();
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
    public void writeEndElement() {
        try {
            stream.writeEndElement();
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

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
