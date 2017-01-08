package org.xs4j.xmlspitter;

import org.xs4j.XMLNode;
import org.xs4j.xmlslurper.NodeListener;
import org.xs4j.xmlslurper.Slurp;
import org.xs4j.util.NotNull;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.xs4j.xmlspitter.OutputSupplierFactory.GenericOutputSupplier.ILLEGAL_SUPPLIER_ARGUMENT;
import static org.xs4j.xmlspitter.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_ENCODING;
import static org.xs4j.xmlspitter.XMLSpitterFactory.DEFAULT_XML_DOCUMENT_VERSION;
import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 12/8/2016.
 */
public class XMLSpitterImpl implements XMLSpitter {
    private static final String NEWLINE = "\n";
    private static final String INDENT = "    ";

    private static long idFeed;

    private final StreamProvider streamProvider;


    XMLSpitterImpl(StreamProvider streamProvider) {
        idFeed = 0L;

        this.streamProvider = streamProvider;
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
            return streamProvider.getStream(outputStream);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream createStream(@NotNull Writer writer) {
        try {
            return streamProvider.getStream(writer);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
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
                    streams[0] = streamProvider.getStream((Writer)output);
                else if (output instanceof OutputStream)
                    streams[0] = streamProvider.getStream((OutputStream)output);
                else
                    throw new IllegalArgumentException(String.format(ILLEGAL_SUPPLIER_ARGUMENT, OutputSupplier.class.getName(), Writer.class.getName(), OutputStream.class.getName()));

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

    interface StreamProvider {
        XMLStream getStream(OutputStream outputStream) throws XMLStreamException;

        XMLStream getStream(Writer writer) throws XMLStreamException;
    }

    static class InternalStreamProvider implements StreamProvider {
        @Override
        public XMLStream getStream(OutputStream outputStream) throws XMLStreamException {
            return new InternalStream(idFeed++, outputStream);
        }

        @Override
        public XMLStream getStream(Writer writer) throws XMLStreamException {
            return new InternalStream(idFeed++, writer);
        }
    }

    static class StAXStreamProvider implements StreamProvider {
        private final XMLOutputFactory xmlOutputFactory;

        StAXStreamProvider(XMLOutputFactory xmlOutputFactory) {
            this.xmlOutputFactory = xmlOutputFactory;
        }

        @Override
        public XMLStream getStream(OutputStream outputStream) throws XMLStreamException {
            XMLStreamWriter stream = xmlOutputFactory.createXMLStreamWriter(outputStream);

            return new StAXStream(idFeed++, stream);
        }

        @Override
        public XMLStream getStream(Writer writer) throws XMLStreamException {
            XMLStreamWriter stream = xmlOutputFactory.createXMLStreamWriter(writer);

            return new StAXStream(idFeed++, stream);
        }
    }

    @SuppressWarnings("unused")
    private static void formatIndent(XMLStream stream, Deque<XMLNode> descendants) {
        for(XMLNode descendant : descendants)
            stream.writeCharacters(INDENT);
    }
}
