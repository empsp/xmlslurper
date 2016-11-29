package com.tsolutions.xmlslurper;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.NodeNotifier.NodeNotifierData;
import com.tsolutions.xmlslurper.path.SlurpNode;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/8/2016.
 */
public class XMLSlurperFactory {
    public enum ParserType {
        STAX_PARSER, SAX_PARSER
    }

    private static XMLSlurperFactory instance;

    public static XMLSlurperFactory getInstance() {
        if (instance == null)
            instance = new XMLSlurperFactory();

        return instance;
    }

    private boolean isNamespaceAwarenessDisabled;

    private XMLSlurperFactory() {
    }

    public XMLSlurperFactory disableNamespaceAwareness() {
        this.isNamespaceAwarenessDisabled = true;

        return this;
    }

    public XMLSlurper createXMLSlurper() {
        List<NodeNotifierData> findData = new ArrayList<NodeNotifierData>();
        List<NodeNotifierData> findAllData = new ArrayList<NodeNotifierData>();

        NodeFactory nodeFactory = getNodeFactory();
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();
        SlurpFactory slurpFactory = getSlurpFactory(findData, findAllData, slurpAlignmentFactory);
        NodeNotifier nodeNotifier = getNodeNotifier(findData, findAllData);

        SAXSlurper saxSlurper = new SAXSlurper(
                getSaxParserFactory(isNamespaceAwarenessDisabled),
                nodeFactory,
                slurpFactory,
                nodeNotifier,
                getSAXNamespaceSensitiveElementParser(isNamespaceAwarenessDisabled, nodeFactory));

        StAXSlurper staxSlurper = new StAXSlurper(
                getXMLInputFactory(),
                nodeFactory,
                slurpFactory,
                nodeNotifier,
                getStAXNamespaceSensitiveElementParser(isNamespaceAwarenessDisabled, nodeFactory));

        return new LazyEngineSlurper(slurpFactory, nodeNotifier, staxSlurper, saxSlurper);
    }

    public XMLSlurper createXMLSlurper(ParserType parserType) {
        List<NodeNotifierData> findData = new ArrayList<NodeNotifierData>();
        List<NodeNotifierData> findAllData = new ArrayList<NodeNotifierData>();

        NodeFactory nodeFactory = getNodeFactory();
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();
        SlurpFactory slurpFactory = getSlurpFactory(findData, findAllData, slurpAlignmentFactory);
        NodeNotifier nodeNotifier = getNodeNotifier(findData, findAllData);

        switch(parserType) {
            case STAX_PARSER:
                return new StAXSlurper(
                        getXMLInputFactory(),
                        nodeFactory,
                        slurpFactory,
                        nodeNotifier,
                        getStAXNamespaceSensitiveElementParser(isNamespaceAwarenessDisabled, nodeFactory));
            case SAX_PARSER:
                return new SAXSlurper(
                        getSaxParserFactory(isNamespaceAwarenessDisabled),
                        nodeFactory,
                        slurpFactory,
                        nodeNotifier,
                        getSAXNamespaceSensitiveElementParser(isNamespaceAwarenessDisabled, nodeFactory));
        }

        throw new IllegalArgumentException();
    }

    static NodeFactory getNodeFactory() {
        return new NodeFactory() {
            @Override
            XMLNode createNode(long id, String localName, Map<String, String> attributeByName) {
                return new XMLNodeImpl(id, localName, attributeByName);
            }

            @Override
            XMLNode createNode(long id, String namespace, String prefix, String localName, Map<String, String> attributeByName) {
                return new XMLNodeImpl(id, namespace, prefix, localName, attributeByName);
            }
        };
    }

    static NodeNotifier getNodeNotifier(List<NodeNotifierData> findData,
                                        List<NodeNotifierData> findAllData) {
        return new NodeNotifier(findData, findAllData);
    }

    static SlurpAlignmentFactory getSlurpAlignmentFactory() {
        return new SlurpAlignmentFactory();
    }

    static SlurpFactory getSlurpFactory(
            List<NodeNotifierData> findData,
            List<NodeNotifierData> findAllData,
            SlurpAlignmentFactory slurpAlignmentFactory) {
        return new SlurpFactory(findData, findAllData, slurpAlignmentFactory);
    }

    static SAXParserFactory getSaxParserFactory(boolean isNamespaceAwarenessDisabled) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        try {
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setFeature("http://xml.org/sax/features/namespaces", true);

            if (isNamespaceAwarenessDisabled)
                saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            else
                saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return saxParserFactory;
    }

    static XMLInputFactory getXMLInputFactory() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

        return xmlInputFactory;
    }

    static NamespaceSensitiveElementParser getSAXNamespaceSensitiveElementParser(boolean isNamespaceAwarenessDisabled, NodeFactory nodeFactory) {
        if (isNamespaceAwarenessDisabled)
            return new SAXSlurper.SAXNamespaceBlindElementParser(nodeFactory);
        else
            return new SAXSlurper.SAXNamespaceAwareElementParser(nodeFactory);
    }

    static NamespaceSensitiveElementParser getStAXNamespaceSensitiveElementParser(boolean isNamespaceAwarenessDisabled, NodeFactory nodeFactory) {
        if (isNamespaceAwarenessDisabled)
            return new StAXSlurper.StAXNamespaceBlindElementParser(nodeFactory);
        else
            return new StAXSlurper.StAXNamespaceAwareElementParser(nodeFactory);
    }

    public class LazyEngineSlurper implements XMLSlurper {
        private final SlurpFactory slurpFactory;
        private final NodeNotifier nodeNotifier;

        private final StAXSlurper staxSlurper;
        private final SAXSlurper saxSlurper;

        LazyEngineSlurper(SlurpFactory slurpFactory, NodeNotifier nodeNotifier, StAXSlurper staxSlurper, SAXSlurper saxSlurper) {
            this.slurpFactory = slurpFactory;
            this.nodeNotifier = nodeNotifier;
            this.staxSlurper = staxSlurper;
            this.saxSlurper = saxSlurper;
        }

        @Override
        public SlurpNode getNodes() {
            return slurpFactory.createSlurpNode();
        }

        @Override
        public void parse(@NotNull String filepath) throws Exception {
            requireNonNull(filepath);

            useSlurperBasedOnNodeListenerTypes(filepath);
        }

        private void useSlurperBasedOnNodeListenerTypes(@NotNull String filepath) throws Exception {
            if (nodeNotifier.areSingleFindListenersAvailableOnly())
                staxSlurper.parse(filepath);
            else
                saxSlurper.parse(filepath);
        }
    }
}
