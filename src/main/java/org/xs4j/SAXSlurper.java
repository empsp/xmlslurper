package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xs4j.path.SlurpNode;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 11/15/2016.
 */
public class SAXSlurper extends DefaultHandler implements XMLSlurper {
    private static long idFeed;

    private final SAXParserFactory saxParserFactory;
    private final SchemaFactory schemaFactory;
    private final SlurpFactory slurpFactory;
    private final NodeNotifier nodeNotifier;
    private final NamespaceSensitiveElementParser elementParser;

    private InputStream inputStream;
    private SAXParser parser;

    private boolean isOnlyFindDataAvailable;

    SAXSlurper(SAXParserFactory saxParserFactory,
               SchemaFactory schemaFactory,
               SlurpFactory slurpFactory,
               NodeNotifier nodeNotifier,
               NamespaceSensitiveElementParser elementParser) {
        idFeed = 0L;

        this.saxParserFactory = saxParserFactory;
        this.schemaFactory = schemaFactory;
        this.slurpFactory = slurpFactory;
        this.nodeNotifier = nodeNotifier;
        this.elementParser = elementParser;
    }

    @Override
    public SlurpNode getNodes(@Nullable String... nodes) {
        return slurpFactory.createSlurpNode(nodes);
    }

    @Override
    public void parse(@NotNull InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        requireNonNull(inputStream);

        isOnlyFindDataAvailable = nodeNotifier.isOnlyFindDataAvailable();

        this.inputStream = inputStream;
        try {
            saxParserFactory.setSchema(null);

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
    public void parse(@NotNull InputStream inputStream, @NotNull File schemaFile) throws ParserConfigurationException, SAXException, IOException {
        requireNonNull(inputStream);
        requireNonNull(schemaFile);

        isOnlyFindDataAvailable = nodeNotifier.isOnlyFindDataAvailable();

        this.inputStream = inputStream;
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            saxParserFactory.setSchema(schema);

            parser = saxParserFactory.newSAXParser();
            parser.parse(inputStream, this);
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
        XMLNodeFactory.appendText(lastNode, ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        nodeNotifier.onEndNode();

        terminateParsingIfPossible();
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    private void terminateParsingIfPossible() throws SAXException {
        if (isOnlyFindDataAvailable && nodeNotifier.isFindDataEmpty())
            throw new ParsingTerminationException();
    }

    private void close() throws IOException {
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
        private final XMLNodeFactory xmlNodeFactory;

        SAXNamespaceAwareElementParser(XMLNodeFactory xmlNodeFactory) {
            this.xmlNodeFactory = xmlNodeFactory;
        }

        @Override
        XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
            int qNameSeparatorIndex = qName.indexOf(XMLNodeFactory.QNAME_SEPARATOR);
            return xmlNodeFactory.createNode(
                    idFeed++,
                    uri.isEmpty() ? null : uri,
                    qNameSeparatorIndex >= 0 ? qName.substring(0, qNameSeparatorIndex) : null,
                    localName,
                    parseAttributes(attributes));
        }

        private Map<String, String> parseAttributes(Attributes attributes) {
            Map<String, String> attributeByName = new HashMap<String, String>();

            String qName;
            int prefixIndex;
            String attrNamespace;
            for (int index = 0; index < attributes.getLength(); index++) {
                qName = attributes.getQName(index);
                attributeByName.put(qName, attributes.getValue(index));

                prefixIndex = qName.indexOf(XMLNodeFactory.QNAME_SEPARATOR);
                if (prefixIndex > 0 && !qName.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    attrNamespace = XMLNodeFactory.XMLNS_WITH_SEPARATOR + qName.substring(0, prefixIndex);
                    attributeByName.put(attrNamespace, attributes.getURI(index));
                }
            }

            return attributeByName;
        }
    }

    static class SAXNamespaceBlindElementParser extends NamespaceSensitiveElementParser {
        private final XMLNodeFactory xmlNodeFactory;

        SAXNamespaceBlindElementParser(XMLNodeFactory xmlNodeFactory) {
            this.xmlNodeFactory = xmlNodeFactory;
        }

        @Override
        XMLNode parseStartElement(String uri, String localName, String qName, Attributes attributes) {
            return xmlNodeFactory.createNode(idFeed++, localName, parseAttributes(attributes));
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
