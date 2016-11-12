package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.event.SlurpListener;
import com.tsolutions.xmlslurper.path.Node;
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

    private FileInputStream fis;
    private XMLStreamReader parser;

    private Deque<Node> descendants;

    StAXSlurper(XMLInputFactory xmlInputFactory, NodeFactory nodeFactory) {
        this.xmlInputFactory = requireNonNull(xmlInputFactory);
        this.nodeFactory = requireNonNull(nodeFactory);
    }

    @Override
    public SlurpNode parse(@NotNull String filepath) throws XMLStreamException, IOException {
        requireNonNull(filepath);

        close();

        descendants = new LinkedList<Node>();
        fis = new FileInputStream(filepath);
        parser = xmlInputFactory.createXMLStreamReader(fis);

        return new StAXSlurpNode(this, new ArrayList<String>());
    }

    void searchFor(StAXSlurpNode slurpNode, SlurpListener slurpListener) throws XMLStreamException {
        while (parser.hasNext()) {
            parser.next();

            switch (parser.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    onStartElement(parser, slurpNode, slurpListener);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    onCharacters(parser);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    onEndElement(parser, slurpNode, slurpListener);
                    break;
            }
        }
    }

    private void onStartElement(XMLStreamReader parser, StAXSlurpNode slurpNode, SlurpListener slurpListener) {
        Node parent = descendants.peekLast();
        Node child = nodeFactory.createNode(idFeed++, parser.getLocalName().intern(), parseAttributes(parser));
        descendants.addLast(child);

        if (slurpNode.areNodePathsAligned(parent, child, descendants.size()))
            slurpListener.onNode(parent, child);
    }

    private Map<String, String> parseAttributes(XMLStreamReader parser) {
        Map<String, String> attributeByName = new HashMap<String, String>();

        for(int index = 0; index < parser.getAttributeCount(); index++)
            attributeByName.put(parser.getAttributeLocalName(index).intern(), parser.getAttributeValue(index));

        return attributeByName;
    }

    private void onCharacters(XMLStreamReader parser) {
        String text = parser.getText();
        Node previous = descendants.peekLast();

        previous.setText(text);
    }

    private void onEndElement(XMLStreamReader parser, StAXSlurpNode slurpNode, SlurpListener slurpListener) {
        // important to get depthLevel of descendants before any operation
        int depthLevel = descendants.size();
        Node child = descendants.removeLast();
        Node parent = descendants.peekLast();

        if (slurpNode.areNodePathsAligned(parent, child, depthLevel))
            slurpListener.onNode(parent, child);
    }

    @Override
    public void close() throws XMLStreamException, IOException {
        idFeed = 0L;

        if (descendants != null)
            descendants = null;

        if (fis != null)
            fis.close();

        if (parser != null)
            parser.close();
    }
}
