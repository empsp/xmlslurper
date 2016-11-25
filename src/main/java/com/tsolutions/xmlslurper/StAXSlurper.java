package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.path.SlurpNode;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/8/2016.
 */
public class StAXSlurper implements XMLSlurper {
    private static long idFeed;

    private final XMLInputFactory xmlInputFactory;
    private final NodeFactory nodeFactory;
    private final SlurpFactory slurpFactory;

    private FileInputStream fis;
    private XMLStreamReader parser;

    private final NodeNotifier nodeNotifier;

    StAXSlurper(
            XMLInputFactory xmlInputFactory, NodeFactory nodeFactory, SlurpFactory slurpFactory, NodeNotifier nodeNotifier) {
        this.xmlInputFactory = xmlInputFactory;
        this.nodeFactory = nodeFactory;
        this.slurpFactory = slurpFactory;
        this.nodeNotifier = nodeNotifier;
    }

    @Override
    public SlurpNode getNodes() {
        return slurpFactory.createSlurpNode();
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
        XMLNode child = nodeFactory.createNode(idFeed++, parser.getLocalName().intern(), parseAttributes());

        nodeNotifier.onStartNode(child);
    }

    private Map<String, String> parseAttributes() {
        Map<String, String> attributeByName = new HashMap<String, String>();

        for (int index = 0; index < parser.getAttributeCount(); index++)
            attributeByName.put(parser.getAttributeLocalName(index).intern(), parser.getAttributeValue(index));

        return attributeByName;
    }

    private void onCharacters() {
        String text = parser.getText();
        XMLNode previous = nodeNotifier.peekLastDescendant();

        previous.setText(text);
    }

    private void onEndElement() {
        nodeNotifier.onEndNode();
    }

    private void close() throws XMLStreamException, IOException {
        idFeed = 0L;

        nodeNotifier.reset();

        if (fis != null)
            fis.close();

        if (parser != null)
            parser.close();
    }
}