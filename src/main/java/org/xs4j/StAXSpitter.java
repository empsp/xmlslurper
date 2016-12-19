package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xs4j.listener.NodeListener;
import org.xs4j.path.Slurp;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by mturski on 12/8/2016.
 */
public class StAXSpitter implements XMLSpitter {
    private static final String NEWLINE = "\n";
    private static final String INDENT = "    ";

    public static final String DEFAULT_XML_DOCUMENT_VERSION = "1.0";
    public static final String DEFAULT_XML_DOCUMENT_ENCODING = "UTF-8";

    private static long idFeed;

    private final XMLOutputFactory xmlOutputFactory;

    StAXSpitter(XMLOutputFactory xmlOutputFactory) {
        idFeed = 0L;

        this.xmlOutputFactory = xmlOutputFactory;
    }

    @Override
    public void write(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier) {
        startWriteOne(documentNode, contentNodes, outputStreamSupplier, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void write(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version) {
        startWriteOne(documentNode, contentNodes, outputStreamSupplier, DEFAULT_XML_DOCUMENT_ENCODING, version);
    }

    @Override
    public void write(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version, final String encoding) {
        startWriteOne(documentNode, contentNodes, outputStreamSupplier, encoding, version);
    }

    @Override
    public void writeAll(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier) {
        startWriteAll(documentNode, contentNodes, outputStreamSupplier, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void writeAll(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version) {
        startWriteAll(documentNode, contentNodes, outputStreamSupplier, DEFAULT_XML_DOCUMENT_ENCODING, version);
    }

    @Override
    public void writeAll(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version, final String encoding) {
        startWriteAll(documentNode, contentNodes, outputStreamSupplier, encoding, version);
    }

    @Override
    public XMLStream createStream(OutputStream outputStream) {
        try {
            return createStreamAndStartWrite(outputStream, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream createStream(OutputStream outputStream, String version) {
        try {
            return createStreamAndStartWrite(outputStream, DEFAULT_XML_DOCUMENT_ENCODING, version);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream createStream(OutputStream outputStream, String version, String encoding) {
        try {
            return createStreamAndStartWrite(outputStream, encoding, version);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    private XMLStream createStreamAndStartWrite(OutputStream outputStream, String encoding, String version) throws XMLStreamException {
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);
        writer.writeStartDocument(encoding, version);

        return new StAXStream(idFeed++, writer);
    }

    private void startWriteOne(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier osSupplier, final String encoding, final String version) {
        final XMLStream[] streams = new XMLStream[1];
        final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

        documentNode.find(
                new StartDocumentHandler(streams, descendants, osSupplier, encoding, version),
                new EndDocumentHandler(streams, descendants));
        contentNodes.findAll(
                new StartContentHandler(streams, descendants),
                new EndContentHandler(streams, descendants));
    }

    private void startWriteAll(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier osSupplier, final String encoding, final String version) {
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
        private final OutputStreamSupplier osSupplier;
        private final String encoding;
        private final String version;

        private StartDocumentHandler(XMLStream[] streams, Deque<XMLNode> descendants, OutputStreamSupplier osSupplier, String encoding, String version) {
            this.streams = streams;
            this.descendants = descendants;
            this.osSupplier = osSupplier;
            this.encoding = encoding;
            this.version = version;
        }

        @Override
        public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
            try {
                streams[0] = createStreamAndStartWrite(osSupplier.supply(), encoding, version);
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
        public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
            descendants.removeLast();

            streams[0].writeCharacters(NEWLINE);
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
        public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
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
        public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
            descendants.removeLast();

            String characters = node.getText();
            if (characters != null && !characters.isEmpty())
                streams[0].writeCharacters(characters);
            else {
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
