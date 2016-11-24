package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.XMLSlurperFactory.SlurpAlignmentListenerTuple;
import com.tsolutions.xmlslurper.listener.NodeListener;
import com.tsolutions.xmlslurper.path.SlurpNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by mturski on 11/15/2016.
 */
public class SAXSlurper extends DefaultHandler implements XMLSlurper {
    private static long idFeed;

    private final SAXParserFactory saxParserFactory;
    private final NodeFactory nodeFactory;
    private final SlurpFactory slurpFactory;

    private FileInputStream fis;
    private SAXParser parser;

    private Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();
    private List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples;

    SAXSlurper(
            SAXParserFactory saxParserFactory, NodeFactory nodeFactory, SlurpFactory slurpFactory, List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples) {
        this.saxParserFactory = saxParserFactory;
        this.nodeFactory = nodeFactory;
        this.slurpFactory = slurpFactory;
        this.slurpAlignmentListenerTuples = slurpAlignmentListenerTuples;
    }

    @Override
    public SlurpNode getNodes() {
        return slurpFactory.createSlurpNode();
    }

    @Override
    public void parse(@NotNull String filepath) throws Exception {
        fis = new FileInputStream(filepath);
        parser = saxParserFactory.newSAXParser();

        parser.parse(fis, this);

        close();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        XMLNode parent = descendants.peekLast();
        XMLNode child = nodeFactory.createNode(idFeed++, qName.intern(), parseAttributes(attributes));

        for (SlurpAlignmentListenerTuple tuple : slurpAlignmentListenerTuples) {
            NodeListener startNodeListener = tuple.getStartNodeListener();

            if(startNodeListener != null && tuple.getSlurpAlignment().checkAlignment(descendants, child))
                startNodeListener.onNode(parent, child);
        }


        descendants.addLast(child);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String text = new String(ch, start, length);
        XMLNode previous = descendants.peekLast();

        previous.setText(text);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        XMLNode child = descendants.removeLast();
        XMLNode parent = descendants.peekLast();

        for (SlurpAlignmentListenerTuple tuple : slurpAlignmentListenerTuples) {
            NodeListener endNodeListener = tuple.getEndNodeListener();

            if(endNodeListener != null && tuple.getSlurpAlignment().checkAlignment(descendants, child))
                endNodeListener.onNode(parent, child);
        }
    }

    private Map<String, String> parseAttributes(Attributes attributes) {
        Map<String, String> attributeByName = new HashMap<String, String>();

        for (int index = 0; index < attributes.getLength(); index++)
            attributeByName.put(attributes.getQName(index).intern(), attributes.getValue(index));

        return attributeByName;
    }

    private void close() throws XMLStreamException, IOException {
        idFeed = 0L;

        descendants.clear();
        slurpAlignmentListenerTuples.clear();

        if (fis != null)
            fis.close();

        if (parser != null)
            parser = null;
    }
}
