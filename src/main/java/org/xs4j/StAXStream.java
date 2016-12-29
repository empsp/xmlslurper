package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 12/8/2016.
 */
public class StAXStream implements XMLStream {
    private static final String EMPTY_PREFIX = "";

    private final long id;
    private final XMLStreamWriter writer;

    private Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();
    private Map<String, String> namespaceByPrefix = new HashMap<String, String>();

    StAXStream(long id, XMLStreamWriter writer) {
        this.id = id;
        this.writer = writer;
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
    public void writeStartElement(@NotNull XMLNode node) {
        try {
            doWriteStartElement(node);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    private void doWriteStartElement(XMLNode node) throws XMLStreamException {
        requireNonNull(node);

        descendants.addLast(node);

        writeStartElementWithNamespaces(node);
        writeAttributesWithNamespaces(node);
    }

    private void writeStartElementWithNamespaces(XMLNode node) throws XMLStreamException {
        String prefix = node.getPrefix();
        String namespace = node.getNamespace();

        if (namespace != null) {
            if (prefix != null) { // handle element names eg. prefix:name
                namespaceByPrefix.put(prefix, namespace);

                writer.writeStartElement(prefix, node.getLocalName(), namespace);
            } else { // handle element names eg. name with default namespace
                if (!namespaceByPrefix.containsKey(EMPTY_PREFIX)) {
                    namespaceByPrefix.put(EMPTY_PREFIX, namespace);

                    writer.writeStartElement(node.getLocalName());
                    writer.writeDefaultNamespace(namespace);
                } else {
                    writer.writeStartElement(EMPTY_PREFIX, node.getLocalName(), namespace);
                }
            }
        } else // handle element names eg. name without default namespace
            writer.writeStartElement(node.getLocalName());
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

                        writer.writeNamespace(prefix, value);
                    }
                } else { // handle attribute names eg. prefix:name
                    prefix = name.substring(0, separatorIndex);

                    if(!namespaceByPrefix.containsKey(prefix)) { // if new
                        namespace = findNamespaceByPrefix(prefix, node.getAttributes()); // the namespace is yet to be extracted from attributes
                        namespaceByPrefix.put(prefix, namespace);

                        writer.writeNamespace(prefix, namespace);
                    } else
                        namespace = namespaceByPrefix.get(prefix);

                    writer.writeAttribute(prefix, namespace, name.substring(separatorIndex + 1), value);
                }
            } else if (name.startsWith(XMLConstants.XMLNS_ATTRIBUTE) && !namespaceByPrefix.containsKey(EMPTY_PREFIX)) { // handle attribute names eg. xlmns, if new
                namespaceByPrefix.put(EMPTY_PREFIX, value);

                writer.writeDefaultNamespace(value);
            } else // handle attribute names eg. name
                writer.writeAttribute(name, value);
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
    public void writeEndElement(@NotNull XMLNode node) {
        requireNonNull(node);

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
