package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.listener.NodeListener;
import org.xs4j.path.Slurp;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.xs4j.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_ENCODING;
import static org.xs4j.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_VERSION;
import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 12/8/2016.
 */
public class StAXSpitter implements XMLSpitter {
    private static final String NEWLINE = "\n";
    private static final String INDENT = "    ";

    private static long idFeed;

    private final XMLOutputFactory xmlOutputFactory;

    StAXSpitter(XMLOutputFactory xmlOutputFactory) {
        idFeed = 0L;

        this.xmlOutputFactory = xmlOutputFactory;
    }

    @Override
    public void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier) {
        startWriteOne(documentNode, contentNodes, outputSupplier, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, @NotNull final String version) {
        startWriteOne(documentNode, contentNodes, outputSupplier, DEFAULT_XML_DOCUMENT_ENCODING, version);
    }

    @Override
    public void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, @NotNull final String version, @NotNull final String encoding) {
        startWriteOne(documentNode, contentNodes, outputSupplier, encoding, version);
    }

    @Override
    public void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier) {
        startWriteAll(documentNode, contentNodes, outputSupplier, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, final String version) {
        startWriteAll(documentNode, contentNodes, outputSupplier, DEFAULT_XML_DOCUMENT_ENCODING, version);
    }

    @Override
    public void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, @NotNull final String version, @NotNull final String encoding) {
        startWriteAll(documentNode, contentNodes, outputSupplier, encoding, version);
    }

    @Override
    public XMLStream createStream(@NotNull OutputStream outputStream) {
        try {
            return createStreamAndStartWrite(outputStream);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream createStream(@NotNull Writer writer) {
        try {
            return createStreamAndStartWrite(writer);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @SuppressWarnings("Duplicates")
    private XMLStream createStreamAndStartWrite(OutputStream outputStream) throws XMLStreamException {
        requireNonNull(outputStream);

        XMLStreamWriter stream = xmlOutputFactory.createXMLStreamWriter(outputStream);

        return new StAXStream(idFeed++, stream);
    }

    @SuppressWarnings("Duplicates")
    private XMLStream createStreamAndStartWrite(Writer writer) throws XMLStreamException {
        requireNonNull(writer);

        XMLStreamWriter stream = xmlOutputFactory.createXMLStreamWriter(writer);

        return new StAXStream(idFeed++, stream);
    }

    private void startWriteOne(Slurp documentNode, Slurp contentNodes, final OutputSupplier<?> osSupplier, final String encoding, final String version) {
        requireNonNull(documentNode);
        requireNonNull(contentNodes);
        requireNonNull(osSupplier);
        requireNonNull(encoding);
        requireNonNull(version);

        final XMLStream[] streams = new XMLStream[1];
        final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

        documentNode.find(
                new StartDocumentHandler(streams, descendants, osSupplier, encoding, version),
                new EndDocumentHandler(streams, descendants));
        contentNodes.findAll(
                new StartContentHandler(streams, descendants),
                new EndContentHandler(streams, descendants));
    }

    private void startWriteAll(Slurp documentNode, Slurp contentNodes, final OutputSupplier<?> osSupplier, final String encoding, final String version) {
        requireNonNull(documentNode);
        requireNonNull(contentNodes);
        requireNonNull(osSupplier);
        requireNonNull(encoding);
        requireNonNull(version);

        final XMLStream[] streams = new XMLStream[1];
        final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

        documentNode.findAll(
                new StartDocumentHandler(streams, descendants, osSupplier, encoding, version),
                new EndDocumentHandler(streams, descendants));
        contentNodes.findAll(
                new StartContentHandler(streams, descendants),
                new EndContentHandler(streams, descendants));
    }

    private class StartDocumentHandler implements NodeListener {
        private final XMLStream[] streams;
        private final Deque<XMLNode> descendants;
        private final OutputSupplier<?> osSupplier;
        private final String encoding;
        private final String version;

        private StartDocumentHandler(XMLStream[] streams, Deque<XMLNode> descendants, OutputSupplier<?> osSupplier, String encoding, String version) {
            this.streams = streams;
            this.descendants = descendants;
            this.osSupplier = osSupplier;
            this.encoding = encoding;
            this.version = version;
        }

        @Override
        public void onNode(@NotNull XMLNode node) {
            try {
                Object output = osSupplier.supply();
                if (output instanceof Writer)
                    streams[0] = createStreamAndStartWrite((Writer)output);
                else
                    streams[0] = createStreamAndStartWrite((OutputStream)output);

                streams[0].writeStartDocument(encoding, version);
                streams[0].writeCharacters(NEWLINE);
                streams[0].writeStartElement(node);

                descendants.addLast(node);
            } catch (XMLStreamException e) {
                throw new XMLStreamRuntimeException(e);
            }
        }
    }

    private class EndDocumentHandler implements NodeListener {
        private final XMLStream[] streams;
        private final Deque<XMLNode> descendants;

        private EndDocumentHandler(XMLStream[] streams, Deque<XMLNode> descendants) {
            this.streams = streams;
            this.descendants = descendants;
        }

        @Override
        public void onNode(@NotNull XMLNode node) {
            descendants.removeLast();

            String characters = node.getText();
            if (characters != null && !characters.isEmpty()) {
                streams[0].writeCharacters(characters);
            } else {
                streams[0].writeCharacters(NEWLINE);
            }
            streams[0].writeEndElement();
            streams[0].close();
            streams[0] = null;
        }
    }

    private class StartContentHandler implements NodeListener {
        private final XMLStream[] streams;
        private final Deque<XMLNode> descendants;

        private StartContentHandler(XMLStream[] streams, Deque<XMLNode> descendants) {
            this.streams = streams;
            this.descendants = descendants;
        }

        @Override
        public void onNode(@NotNull XMLNode node) {
            streams[0].writeCharacters(NEWLINE);
            formatIndent(streams[0], descendants);
            streams[0].writeStartElement(node);

            descendants.addLast(node);
        }
    }

    private class EndContentHandler implements NodeListener {
        private final XMLStream[] streams;
        private final Deque<XMLNode> descendants;

        private EndContentHandler(XMLStream[] streams, Deque<XMLNode> descendants) {
            this.streams = streams;
            this.descendants = descendants;
        }

        @Override
        public void onNode(@NotNull XMLNode node) {
            descendants.removeLast();

            String characters = node.getText();
            if (characters != null && !characters.isEmpty()) {
                streams[0].writeCharacters(characters);
            } else {
                streams[0].writeCharacters(NEWLINE);
                formatIndent(streams[0], descendants);
            }
            streams[0].writeEndElement();
            streams[0].flush();
        }
    }

    @SuppressWarnings("unused")
    private static void formatIndent(XMLStream stream, Deque<XMLNode> descendants) {
        for(XMLNode descendant : descendants)
            stream.writeCharacters(INDENT);
    }
}
