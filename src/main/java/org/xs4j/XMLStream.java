package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import java.io.OutputStream;

/**
 * Defines an API for node based XML document writer.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface XMLStream {
    /**
     * Id identifying current {@link XMLStream} instance.
     *
     * @return id unique to the scope of single <code>XMLSpitter</code> instance
     */
    long getId();

    /**
     * Write simple (not having descendants) element containing start-tag (with attributes), text, end-tag.
     *
     * @param node to retrieve data from
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeElement(@NotNull XMLNode node);

    /**
     * Write start-tag (with attributes) of an element.
     *
     * @param node to retrieve data from
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeStartElement(@NotNull XMLNode node);

    /**
     * Write text of the element currently opened with start-tag.
     *
     * @param characters to write
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeCharacters(@Nullable String characters);

    /**
     * Write end-tag of the element opened with start-tag.
     *
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeEndElement();

    /**
     * Write multiple end-tag closing all opened elements until end-tag of {@link XMLNode} is found.
     *
     * @param node to match the end-tag
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeEndElement(@NotNull XMLNode node);

    /**
     * Flush the content into the underlying {@link OutputStream}.
     */
    void flush();

    /**
     * Close underlying {@link OutputStream}.
     */
    void close();
}
