package org.xs4j.xmlspitter;

import org.xml.sax.SAXException;
import org.xs4j.XMLNode;
import org.xs4j.xmlslurper.NodeListener;
import org.xs4j.util.NotNull;
import org.xs4j.xmlslurper.XMLSlurper;
import org.xs4j.xmlspitter.XMLSpitter;
import org.xs4j.xmlspitter.XMLStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.xs4j.TestUtil.getResource;
import static org.xs4j.TestUtil.getResourceAsFile;

/**
 * Created by mturski on 1/7/2017.
 */
public class SpitterUtil {
    private final XMLSlurper xmlSlurper;
    private final XMLSpitter xmlSpitter;

    public SpitterUtil(XMLSlurper xmlSlurper, XMLSpitter xmlSpitter) {
        this.xmlSlurper = xmlSlurper;
        this.xmlSpitter = xmlSpitter;
    }

    public void createXML(
            final OutputStream outputStream,
            List<String> parentPath,
            List<String> descendantsPath,
            String resourceName) throws ParserConfigurationException, XMLStreamException, SAXException, IOException {

        configureParser(outputStream, parentPath, descendantsPath);

        xmlSlurper.parse(getResource(this, resourceName));
    }

    public void createXML(
            final OutputStream outputStream,
            List<String> parentPath,
            List<String> descendantsPath,
            String resourceName,
            String schemaName) throws ParserConfigurationException, XMLStreamException, SAXException, IOException {

        configureParser(outputStream, parentPath, descendantsPath);

        xmlSlurper.parse(getResource(this, resourceName), getResourceAsFile(this, schemaName));
    }

    public void configureParser(
            final OutputStream outputStream,
            List<String> parentPath,
            List<String> descendantsPath) {

        final XMLStream[] streams = new XMLStream[1];

        xmlSlurper.getNodes(parentPath.toArray(new String[parentPath.size()])).find(new NodeListener() {
            @Override
            public void onNode(@NotNull XMLNode node) {
                streams[0] = xmlSpitter.createStream(outputStream);
                streams[0].writeStartDocument();
                streams[0].writeStartElement(node);
            }
        }, new NodeListener() {
            @Override
            public void onNode(@NotNull XMLNode node) {
                streams[0].writeEndElement();
                streams[0].flush();
                streams[0].close();
            }
        });
        xmlSlurper.getNodes(descendantsPath.toArray(new String[descendantsPath.size()])).findAll(new NodeListener() {
            @Override
            public void onNode(@NotNull XMLNode node) {
                streams[0].writeStartElement(node);
            }
        }, new NodeListener() {
            @Override
            public void onNode(@NotNull XMLNode node) {
                streams[0].writeCharacters(node);
                streams[0].writeEndElement();
            }
        });
    }
}
