package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.listener.SlurpListener;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
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
        return new SAXSlurper(SAXParserFactory.newInstance(), getNodeFactory(), getSlurpAlignmentFactory());
    }

    public XMLSlurper createXMLSlurper(ParserType parserType) {
        switch(parserType) {
            case STAX_PARSER:
                return new StAXSlurper(XMLInputFactory.newInstance(), getNodeFactory(), getSlurpAlignmentFactory());
            case SAX_PARSER:
                return new SAXSlurper(SAXParserFactory.newInstance(), getNodeFactory(), getSlurpAlignmentFactory());
        }

        throw new IllegalArgumentException();
    }

    static NodeFactory getNodeFactory() {
        return new NodeFactory() {
            @Override
            XMLNode createNode(long id, String name, Map<String, String> attributeByName) {
                return new XMLNodeImpl(id, name, attributeByName);
            }
        };
    }

    static SlurpAlignmentFactory getSlurpAlignmentFactory() {
        return new SlurpAlignmentFactory();
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
