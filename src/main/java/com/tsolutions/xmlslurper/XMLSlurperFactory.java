package com.tsolutions.xmlslurper;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.NodeNotifier.SlurpAlignmentListenerTuple;
import com.tsolutions.xmlslurper.path.SlurpNode;

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

    private XMLSlurperFactory() {
    }

    public XMLSlurper createXMLSlurper() {
        List<SlurpAlignmentListenerTuple> findTuples = new ArrayList<SlurpAlignmentListenerTuple>();
        List<SlurpAlignmentListenerTuple> findAllTuples = new ArrayList<SlurpAlignmentListenerTuple>();

        NodeNotifier nodeNotifier = new NodeNotifier(findTuples, findAllTuples);
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();
        SlurpFactory slurpFactory = getSlurpFactory(findTuples, findAllTuples, slurpAlignmentFactory);
        NodeFactory nodeFactory = getNodeFactory();

        SAXSlurper saxSlurper = new SAXSlurper(SAXParserFactory.newInstance(), nodeFactory, slurpFactory, nodeNotifier);
        StAXSlurper staxSlurper = new StAXSlurper(XMLInputFactory.newInstance(), nodeFactory, slurpFactory, nodeNotifier);

        return new LazyEngineSlurper(slurpFactory, nodeNotifier, staxSlurper, saxSlurper);
    }

    public XMLSlurper createXMLSlurper(ParserType parserType) {
        List<SlurpAlignmentListenerTuple> findTuples = new ArrayList<SlurpAlignmentListenerTuple>();
        List<SlurpAlignmentListenerTuple> findAllTuples = new ArrayList<SlurpAlignmentListenerTuple>();

        NodeNotifier nodeNotifier = new NodeNotifier(findTuples, findAllTuples);
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();
        SlurpFactory slurpFactory = getSlurpFactory(findTuples, findAllTuples, slurpAlignmentFactory);
        NodeFactory nodeFactory = getNodeFactory();

        switch(parserType) {
            case STAX_PARSER:
                return new StAXSlurper(XMLInputFactory.newInstance(), nodeFactory, slurpFactory, nodeNotifier);
            case SAX_PARSER:
                return new SAXSlurper(SAXParserFactory.newInstance(), nodeFactory, slurpFactory, nodeNotifier);
        }

        throw new IllegalArgumentException();
    }

    static NodeFactory getNodeFactory() {
        return new NodeFactory() {
            @Override
            XMLNode createNode(long id, String localName, Map<String, String> attributeByName) {
                return new XMLNodeImpl(id, localName, attributeByName);
            }
        };
    }

    static SlurpAlignmentFactory getSlurpAlignmentFactory() {
        return new SlurpAlignmentFactory();
    }

    private static SlurpFactory getSlurpFactory(
            List<SlurpAlignmentListenerTuple> findTuples,
            List<SlurpAlignmentListenerTuple> findAllTuples,
            SlurpAlignmentFactory slurpAlignmentFactory) {
        return new SlurpFactory(findTuples, findAllTuples, slurpAlignmentFactory);
    }

    public class LazyEngineSlurper implements XMLSlurper {
        private final SlurpFactory slurpFactory;
        private final NodeNotifier nodeNotifier;

        private final StAXSlurper staxSlurper;
        private final SAXSlurper saxSlurper;

        public LazyEngineSlurper(SlurpFactory slurpFactory, NodeNotifier nodeNotifier, StAXSlurper staxSlurper, SAXSlurper saxSlurper) {
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
