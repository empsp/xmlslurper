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
    private static final String NEWLINE_AND_INDENT = NEWLINE + INDENT;

    public static final String DEFAULT_XML_DOCUMENT_VERSION = "1.0";
    public static final String DEFAULT_XML_DOCUMENT_ENCODING = "UTF-8";

    private static long idFeed;

    private final XMLOutputFactory xmlOutputFactory;

    StAXSpitter(XMLOutputFactory xmlOutputFactory) {
        idFeed = 0L;

        this.xmlOutputFactory = xmlOutputFactory;
    }

    @Override
    public XMLStream write(OutputStream outputStream) {
        try {
            return startWriting(outputStream, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream write(OutputStream outputStream, String version) {
        try {
            return startWriting(outputStream, DEFAULT_XML_DOCUMENT_ENCODING, version);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public XMLStream write(OutputStream outputStream, String version, String encoding) {
        try {
            return startWriting(outputStream, encoding, version);
        } catch (XMLStreamException e) {
            throw new XMLStreamRuntimeException(e);
        }
    }

    @Override
    public void write(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier) {
        startWriting(documentNode, contentNodes, outputStreamSupplier, DEFAULT_XML_DOCUMENT_ENCODING, DEFAULT_XML_DOCUMENT_VERSION);
    }

    @Override
    public void write(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version) {
        startWriting(documentNode, contentNodes, outputStreamSupplier, DEFAULT_XML_DOCUMENT_ENCODING, version);
    }

    @Override
    public void write(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version, final String encoding) {
        startWriting(documentNode, contentNodes, outputStreamSupplier, encoding, version);
    }

    private XMLStream startWriting(OutputStream outputStream, String encoding, String version) throws XMLStreamException {
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);
        writer.writeStartDocument(encoding, version);

        return new StAXStream(idFeed++, writer);
    }

    private void startWriting(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String encoding, final String version) {
        final XMLStream[] streams = new XMLStream[1];
        final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

        documentNode.findAll(new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                try {
                    streams[0] = startWriting(outputStreamSupplier.supply(), encoding, version);
                    streams[0].writeCharacters(NEWLINE);
                    streams[0].writeStartElement(node);

                    descendants.addLast(node);
                } catch (XMLStreamException e) {
                    throw new XMLStreamRuntimeException(e);
                }
            }
        }, new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                descendants.removeLast();

                streams[0].writeCharacters(NEWLINE);
                streams[0].writeEndElement();
                streams[0].close();
                streams[0] = null;
            }
        });

        contentNodes.findAll(new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                streams[0].writeCharacters(NEWLINE);
                formatIndent(streams[0], descendants);
                streams[0].writeStartElement(node);

                descendants.addLast(node);
            }
        }, new NodeListener() {
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
        });
    }

    private static void formatIndent(XMLStream stream, Deque<XMLNode> descendants) {
        for(XMLNode descendant : descendants)
            stream.writeCharacters(INDENT);
    }
}
