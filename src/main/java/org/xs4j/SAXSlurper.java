package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xs4j.path.SlurpNode;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.xs4j.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/15/2016.
 */
public class SAXSlurper extends DefaultHandler implements XMLSlurper {
    private static long idFeed;

    private final SAXParserFactory saxParserFactory;
    private final NodeFactory nodeFactory;
    private final SlurpFactory slurpFactory;
    private final NodeNotifier nodeNotifier;
    private final NamespaceSensitiveElementParser elementParser;

    private InputStream inputStream;
    private SAXParser parser;

    private boolean isOnlyFindDataAvailable;

    SAXSlurper(SAXParserFactory saxParserFactory, NodeFactory nodeFactory, SlurpFactory slurpFactory, NodeNotifier nodeNotifier, NamespaceSensitiveElementParser elementParser) {
        this.saxParserFactory = saxParserFactory;
        this.nodeFactory = nodeFactory;
        this.slurpFactory = slurpFactory;
        this.nodeNotifier = nodeNotifier;
        this.elementParser = elementParser;
    }

    @Override
    public SlurpNode getNodes(@Nullable String... nodes) {
        return slurpFactory.createSlurpNode(nodes);
    }

    @Override
    public void parse(@NotNull InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
        requireNonNull(inputStream);

        isOnlyFindDataAvailable = nodeNotifier.isOnlyFindDataAvailable();

        this.inputStream = inputStream;
        try {
            parser = saxParserFactory.newSAXParser();
            parser.parse(inputStream, this);
        } catch (ParserConfigurationException e) {
            throw e;
        } catch (ParsingTerminationException e) {
            // do not rethrow
        } catch (SAXException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            close();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        nodeNotifier.onStartNode(elementParser.parseStartElement(uri, localName, qName, attributes));

        terminateParsingIfPossible();
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        XMLNode lastNode = nodeNotifier.peekLastDescendant();
        String text = new String(ch, start, length);
        if (text != null) {
            String lastText = lastNode.getText();
            lastNode.setText(lastText == null ? text : lastText + text);
        }

        terminateParsingIfPossible();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        nodeNotifier.onEndNode();

        terminateParsingIfPossible();
    }

    private void terminateParsingIfPossible() throws SAXException {
        if (isOnlyFindDataAvailable && nodeNotifier.isFindDataEmpty())
            throw new ParsingTerminationException();
    }

    private void close() throws XMLStreamException, IOException {
        idFeed = 0L;

        nodeNotifier.reset();

        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }

        if (parser != null)
            parser = null;
    }

    static class SAXNamespaceAwareElementParser extends NamespaceSensitiveElementParser {
        SAXNamespaceAwareElementParser(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
            int qNameSeparatorIndex = qName.indexOf(NodeFactory.QNAME_SEPARATOR);
            return nodeFactory.createNode(
                    idFeed++,
                    uri.isEmpty() ? null : uri,
                    qNameSeparatorIndex >= 0 ? qName.substring(0, qNameSeparatorIndex) : null,
                    localName,
                    parseAttributes(attributes));
        }

        private Map<String, String> parseAttributes(Attributes attributes) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < attributes.getLength(); index++)
                attributeByName.put(attributes.getQName(index), attributes.getValue(index));

            return attributeByName;
        }
    }

    static class SAXNamespaceBlindElementParser extends NamespaceSensitiveElementParser {
        SAXNamespaceBlindElementParser(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
            return nodeFactory.createNode(idFeed++, localName, parseAttributes(attributes));
        }

        private Map<String, String> parseAttributes(Attributes attributes) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < attributes.getLength(); index++)
                attributeByName.put(attributes.getLocalName(index), attributes.getValue(index));

            return attributeByName;
        }
    }

    static class ParsingTerminationException extends SAXException {
    }
}
