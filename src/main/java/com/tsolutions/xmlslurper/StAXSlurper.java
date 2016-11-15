package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.SlurpNode;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/8/2016.
 */
public class StAXSlurper implements XMLSlurper {
    private static long idFeed;

    private final XMLInputFactory xmlInputFactory;
    private final NodeFactory nodeFactory;
    private final SlurpAlignmentFactory slurpAlignmentFactory;

    private FileInputStream fis;
    private XMLStreamReader parser;

    private Deque<XMLNode> descendants = new LinkedList<XMLNode>();
    private Map<SlurpAlignment, SlurpListener> slurpAlignmentListenerTuples = new HashMap<SlurpAlignment, SlurpListener>();

    StAXSlurper(XMLInputFactory xmlInputFactory, NodeFactory nodeFactory, SlurpAlignmentFactory slurpAlignmentFactory) {
        this.xmlInputFactory = xmlInputFactory;
        this.nodeFactory = nodeFactory;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
    }

    @Override
    public SlurpNode getNodes() {
        return new SlurpNodeImpl(slurpAlignmentListenerTuples, slurpAlignmentFactory, slurpAlignmentFactory.createEmpty());
    }

    @Override
    public void parse(@NotNull String filepath) throws Exception {
        requireNonNull(filepath);

        fis = new FileInputStream(filepath);
        parser = xmlInputFactory.createXMLStreamReader(fis);

        while (parser.hasNext()) {
            parser.next();

            switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    onStartElement();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    onCharacters();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    onEndElement();
                    break;
            }
        }

        close();
    }

    private void onStartElement() {
        XMLNode parent = descendants.peekLast();
        XMLNode child = nodeFactory.createNode(idFeed++, parser.getLocalName().intern(), parseAttributes());
        descendants.addLast(child);

        for (Map.Entry<SlurpAlignment, SlurpListener> tuple : slurpAlignmentListenerTuples.entrySet())
            if(tuple.getKey().checkAlignment(child, descendants.size()))
                tuple.getValue().onNode(parent, child);
    }

    private Map<String, String> parseAttributes() {
        Map<String, String> attributeByName = new HashMap<String, String>();

        for (int index = 0; index < parser.getAttributeCount(); index++)
            attributeByName.put(parser.getAttributeLocalName(index).intern(), parser.getAttributeValue(index));

        return attributeByName;
    }

    private void onCharacters() {
        String text = parser.getText();
        XMLNode previous = descendants.peekLast();

        previous.setText(text);
    }

    private void onEndElement() {
        // important to get depthLevel of descendants before any operation
        int depthLevel = descendants.size();
        XMLNode child = descendants.removeLast();
        XMLNode parent = descendants.peekLast();

        for (Map.Entry<SlurpAlignment, SlurpListener> tuple : slurpAlignmentListenerTuples.entrySet())
            if(tuple.getKey().checkAlignment(child, depthLevel))
                tuple.getValue().onNode(parent, child);
    }

    private void close() throws XMLStreamException, IOException {
        idFeed = 0L;

        descendants.clear();
        slurpAlignmentListenerTuples.clear();

        if (fis != null)
            fis.close();

        if (parser != null)
            parser.close();
    }
}