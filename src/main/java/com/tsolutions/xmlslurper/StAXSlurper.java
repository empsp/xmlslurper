package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.path.SlurpNode;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.tsolutions.xmlslurper.NodeFactory.QNAME_SEPARATOR;
import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

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

    private FileInputStream inputStream;
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
    public void parse(@NotNull String filepath) throws IOException, XMLStreamException {
        requireNonNull(filepath);

        findNodeListenersOnlyMode = nodeNotifier.areSingleFindListenersAvailableOnly();

        inputStream = new FileInputStream(filepath);
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
                    onStartElement(parser);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    onCharacters();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    onEndElement();
                    break;
            }

            if (findNodeListenersOnlyMode && nodeNotifier.areSingleFindListenersEmpty())
                break;
        }
    }

    private void onStartElement(XMLStreamReader parser) {
        nodeNotifier.onStartNode(elementParser.parseStartElement(parser));
    }

    private void onCharacters() {
        String text = parser.getText();
        XMLNode previous = nodeNotifier.peekLastDescendant();

        previous.setText(text);
    }

    private void onEndElement() {
        nodeNotifier.onEndNode();
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
                    idFeed++, parser.getNamespaceURI(), prefix.isEmpty() ? null : prefix.intern(), parser.getLocalName().intern(), parseAttributes(parser));
        }

        private Map<String, String> parseAttributes(XMLStreamReader parser) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < parser.getAttributeCount(); index++) {
                String prefix = parser.getAttributePrefix(index);
                attributeByName.put(
                        prefix.isEmpty() ? parser.getAttributeLocalName(index).intern() : prefix.concat(QNAME_SEPARATOR).concat(parser.getAttributeLocalName(index)).intern(),
                        parser.getAttributeValue(index));
            }

            for(int index = 0; index < parser.getNamespaceCount(); index++)
                attributeByName.put(XMLConstants.XMLNS_ATTRIBUTE + QNAME_SEPARATOR + parser.getNamespacePrefix(index), parser.getNamespaceURI(index));

            return attributeByName;
        }
    }

    static class StAXNamespaceBlindElementParser extends NamespaceSensitiveElementParser {
        StAXNamespaceBlindElementParser(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        XMLNode parseStartElement(XMLStreamReader parser) {
            return nodeFactory.createNode(idFeed++, parser.getLocalName().intern(), parseAttributes(parser));
        }

        private Map<String, String> parseAttributes(XMLStreamReader parser) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < parser.getAttributeCount(); index++)
                attributeByName.put(parser.getAttributeLocalName(index).intern(), parser.getAttributeValue(index));

            return attributeByName;
        }
    }
}