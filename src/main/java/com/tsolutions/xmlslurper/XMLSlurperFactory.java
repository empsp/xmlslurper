package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.listener.SlurpListener;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public final class XMLSlurperFactory {
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
        List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples = new ArrayList<SlurpAlignmentListenerTuple>();
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();

        return new SAXSlurper(
                SAXParserFactory.newInstance(), getNodeFactory(), getSlurpFactory(slurpAlignmentListenerTuples, slurpAlignmentFactory), slurpAlignmentListenerTuples);
    }

    public XMLSlurper createXMLSlurper(ParserType parserType) {
        List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples = new ArrayList<SlurpAlignmentListenerTuple>();
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();
        SlurpFactory slurpFactory = getSlurpFactory(slurpAlignmentListenerTuples, slurpAlignmentFactory);

        NodeFactory nodeFactory = getNodeFactory();

        switch(parserType) {
            case STAX_PARSER:
                return new StAXSlurper(XMLInputFactory.newInstance(), nodeFactory, slurpFactory, slurpAlignmentListenerTuples);
            case SAX_PARSER:
                return new SAXSlurper(SAXParserFactory.newInstance(), nodeFactory, slurpFactory, slurpAlignmentListenerTuples);
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
            List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples, SlurpAlignmentFactory slurpAlignmentFactory) {
        return new SlurpFactory(slurpAlignmentFactory, slurpAlignmentListenerTuples);
    }

    static class SlurpAlignmentListenerTuple {
        private final SlurpAlignment slurpAlignment;
        private final SlurpListener slurpListener;

        SlurpAlignmentListenerTuple(SlurpAlignment slurpAlignment, SlurpListener slurpListener) {
            this.slurpAlignment = slurpAlignment;
            this.slurpListener = slurpListener;
        }

        SlurpAlignment getSlurpAlignment() {
            return slurpAlignment;
        }

        SlurpListener getSlurpListener() {
            return slurpListener;
        }
    }
}
