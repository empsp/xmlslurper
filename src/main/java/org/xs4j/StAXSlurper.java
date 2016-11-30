package org.xs4j;

import com.sun.istack.NotNull;
import org.xs4j.path.SlurpNode;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static org.xs4j.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/8/2016.
 */
public class StAXSlurper implements XMLSlurper {
    private static long idFeed;

    private final XMLInputFactory xmlInputFactory;
    private final NodeFactory nodeFactory;
    private final SlurpFactory slurpFactory;
    private final NodeNotifier nodeNotifier;
    private final NamespaceSensitiveElementParser elementParser;

    private InputStream inputStream;
    private XMLStreamReader parser;

    private boolean findNodeListenersOnlyMode;

    StAXSlurper(
            XMLInputFactory xmlInputFactory,
            NodeFactory nodeFactory,
            SlurpFactory slurpFactory,
            NodeNotifier nodeNotifier,
            NamespaceSensitiveElementParser elementParser) {
        this.xmlInputFactory = xmlInputFactory;
        this.nodeFactory = nodeFactory;
        this.slurpFactory = slurpFactory;
        this.nodeNotifier = nodeNotifier;
        this.elementParser = elementParser;
    }

    @Override
    public SlurpNode getNodes() {
        return slurpFactory.createSlurpNode();
    }

    @Override
    @Deprecated
    public void parse(@NotNull String filepath) throws XMLStreamException, IOException {
        requireNonNull(filepath);

        parse(getInputStreamBasedOnFileType(filepath));
    }

    @Override
    public void parse(@NotNull InputStream inputStream) throws XMLStreamException, IOException {
        requireNonNull(inputStream);

        findNodeListenersOnlyMode = nodeNotifier.areSingleFindListenersAvailableOnly();

        this.inputStream = inputStream;
        try {
            parser = xmlInputFactory.createXMLStreamReader(inputStream);
            doParse(parser);
        } catch (XMLStreamException e) {
            throw e;
        } finally {
            close();
        }
    }

    private void doParse(XMLStreamReader parser) throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();

            switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    nodeNotifier.onStartNode(elementParser.parseStartElement(parser));
                    break;
                case XMLStreamConstants.CHARACTERS:
                    XMLNode lastNode = nodeNotifier.peekLastDescendant();
                    String text = parser.getText();
                    if (text != null) {
                        String lastText = lastNode.getText();
                        lastNode.setText(lastText == null ? text : lastText + text);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    nodeNotifier.onEndNode();
                    break;
            }

            if (findNodeListenersOnlyMode && nodeNotifier.areSingleFindListenersEmpty())
                break;
        }
    }

    private static InputStream getInputStreamBasedOnFileType(String filepath) throws IOException {
        InputStream inputStream = new FileInputStream(filepath);

        try {
            if (filepath.endsWith(".gz"))
                inputStream = new GZIPInputStream(inputStream);
            else if (filepath.endsWith(".zip"))
                inputStream = new ZipInputStream(inputStream);
        } catch (IOException e) {
            inputStream.close();
            throw e;
        }

        return inputStream;
    }

    private void close() throws XMLStreamException, IOException {
        idFeed = 0L;

        nodeNotifier.reset();

        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }

        if (parser != null)
            parser.close();
    }

    static class StAXNamespaceAwareElementParser extends NamespaceSensitiveElementParser {
        StAXNamespaceAwareElementParser(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        XMLNode parseStartElement(XMLStreamReader parser) {
            String prefix = parser.getPrefix();
            return nodeFactory.createNode(
                    idFeed++, parser.getNamespaceURI(), prefix.isEmpty() ? null : prefix, parser.getLocalName(), parseAttributes(parser));
        }

        private Map<String, String> parseAttributes(XMLStreamReader parser) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < parser.getAttributeCount(); index++) {
                String prefix = parser.getAttributePrefix(index);
                attributeByName.put(
                        prefix.isEmpty() ? parser.getAttributeLocalName(index) : prefix + NodeFactory.QNAME_SEPARATOR + parser.getAttributeLocalName(index),
                        parser.getAttributeValue(index));
            }

            String xmlns = XMLConstants.XMLNS_ATTRIBUTE + NodeFactory.QNAME_SEPARATOR;
            for(int index = 0; index < parser.getNamespaceCount(); index++)
                attributeByName.put(xmlns + parser.getNamespacePrefix(index), parser.getNamespaceURI(index));

            return attributeByName;
        }
    }

    static class StAXNamespaceBlindElementParser extends NamespaceSensitiveElementParser {
        StAXNamespaceBlindElementParser(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        XMLNode parseStartElement(XMLStreamReader parser) {
            return nodeFactory.createNode(idFeed++, parser.getLocalName(), parseAttributes(parser));
        }

        private Map<String, String> parseAttributes(XMLStreamReader parser) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < parser.getAttributeCount(); index++)
                attributeByName.put(parser.getAttributeLocalName(index), parser.getAttributeValue(index));

            return attributeByName;
        }
    }
}