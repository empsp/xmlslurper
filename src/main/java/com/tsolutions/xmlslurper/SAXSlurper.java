package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.path.SlurpNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static com.tsolutions.xmlslurper.NodeFactory.QNAME_SEPARATOR;
import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

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

    SAXSlurper(SAXParserFactory saxParserFactory, NodeFactory nodeFactory, SlurpFactory slurpFactory, NodeNotifier nodeNotifier, NamespaceSensitiveElementParser elementParser) {
        this.saxParserFactory = saxParserFactory;
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
    public void parse(@NotNull String filepath) throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
        requireNonNull(filepath);

        try {
            inputStream = getInputStreamBasedOnFileType(filepath);
            parser = saxParserFactory.newSAXParser();
            parser.parse(inputStream, this);
        } catch (ParserConfigurationException e) {
            throw e;
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
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String text = new String(ch, start, length);

        XMLNode lastNode = nodeNotifier.peekLastDescendant();
        lastNode.setText(text);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        nodeNotifier.onEndNode();
    }

    private static InputStream getInputStreamBasedOnFileType(String filepath) throws IOException {
        InputStream inputStream = new FileInputStream(filepath);

        if (filepath.endsWith(".gz"))
            inputStream = new GZIPInputStream(inputStream);
        else if (filepath.endsWith(".zip"))
            inputStream = new ZipInputStream(inputStream);

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
            parser = null;
    }

    static class SAXNamespaceAwareElementParser extends NamespaceSensitiveElementParser {
        SAXNamespaceAwareElementParser(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
            int qNameSeparatorIndex = qName.indexOf(QNAME_SEPARATOR);
            return nodeFactory.createNode(
                    idFeed++,
                    uri,
                    qNameSeparatorIndex >= 0 ? qName.substring(0, qNameSeparatorIndex) : null,
                    localName.intern(),
                    parseAttributes(attributes));
        }

        private Map<String, String> parseAttributes(Attributes attributes) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < attributes.getLength(); index++)
                attributeByName.put(attributes.getQName(index).intern(), attributes.getValue(index));

            return attributeByName;
        }
    }

    static class SAXNamespaceBlindElementParser extends NamespaceSensitiveElementParser {
        SAXNamespaceBlindElementParser(NodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
            return nodeFactory.createNode(idFeed++, localName.intern(), parseAttributes(attributes));
        }

        private Map<String, String> parseAttributes(Attributes attributes) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            for (int index = 0; index < attributes.getLength(); index++)
                attributeByName.put(attributes.getLocalName(index).intern(), attributes.getValue(index));

            return attributeByName;
        }
    }
}
