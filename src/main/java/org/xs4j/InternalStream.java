package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static org.xs4j.XMLNodeFactory.getAttributesDirectly;
import static org.xs4j.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_ENCODING;
import static org.xs4j.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_VERSION;
import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 1/3/2017.
 */
public class InternalStream implements XMLStream {
    private static final String NO_START_FOR_END_ELEMENT_EXCEPTION = "No element was found to write";

    private static final String START_DOCUMENT_PATTERN = "<?xml version=\"%s\" encoding=\"%s\"?>";

    private static final String TAG_OPENING_CHAR = "<";
    private static final String CLOSING_ELEMENT_TAG_OPENING_CHARS = "</";
    private static final String EMPTY_ELEMENT_TAG_CLOSING_CHARS = " />";
    private static final String TAG_CLOSING_CHAR = ">";
    private static final String ATTR_LEADING_SPACE = " ";
    private static final String ATTR_EQUALS_AND_QUOTE = "=\"";
    private static final String ATTR_CLOSING_QUOTE = "\"";

    private final long id;
    private final Writer writer;
    private final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

    InternalStream(long id, Writer writer) {
        this.id = id;
        this.writer = writer;
    }

    InternalStream(long id, OutputStream outputStream) {
        this(id, new OutputStreamWriter(outputStream));
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void writeStartDocument() {
        doWriteStartDocument(DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
    }


    @Override
    public void writeStartDocument(@NotNull String encoding) {
        doWriteStartDocument(encoding, DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void writeStartDocument(@NotNull String encoding, @NotNull String version) {
        doWriteStartDocument(encoding, version);
    }

    private void doWriteStartDocument(String encoding, String version) {
        doWrite(String.format(START_DOCUMENT_PATTERN, version, encoding));
    }

    @Override
    public void writeElement(@NotNull XMLNode node) {
        requireNonNull(node);

        if (node.getText() == null) {
            doWrite(TAG_OPENING_CHAR + node.getQName() + doWriteAttributes(getAttributesDirectly(node)) + EMPTY_ELEMENT_TAG_CLOSING_CHARS);
        } else {
            writeStartElement(node);
            writeCharacters(node.getText());
            writeEndElement();
        }
    }

    private String doWriteAttributes(Map<String, String> attributes) {
        String result = "";

        for (String key : attributes.keySet())
            result += ATTR_LEADING_SPACE + key + ATTR_EQUALS_AND_QUOTE + attributes.get(key) + ATTR_CLOSING_QUOTE;

        return result;
    }

    @Override
    public void writeStartElement(@NotNull XMLNode node) {
        requireNonNull(node);

        doWrite(TAG_OPENING_CHAR + node.getQName() + doWriteAttributes(getAttributesDirectly(node)) + TAG_CLOSING_CHAR);
        descendants.addLast(node);
    }

    @Override
    public void writeCharacters(@Nullable String characters) {
        if (characters != null)
            doWrite(characters);
    }

    @Override
    public void writeEndElement() {
        XMLNode node = descendants.removeLast();
        if (node == null)
            throw new XMLStreamRuntimeException(NO_START_FOR_END_ELEMENT_EXCEPTION);

        doWrite(CLOSING_ELEMENT_TAG_OPENING_CHARS + node.getQName() + TAG_CLOSING_CHAR);
    }

    @Override
    public void writeEndElement(@NotNull XMLNode node) {
        requireNonNull(node);

        doWrite(CLOSING_ELEMENT_TAG_OPENING_CHARS + node.getQName() + TAG_CLOSING_CHAR);
    }

    @Override
    public void flush() {
        doFlush();
    }


    @Override
    public void close() {
        doClose();
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

    private void doWrite(String text) {
        try {
            writer.write(text);
        } catch (IOException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    private void doFlush() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    private void doClose() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }
}
