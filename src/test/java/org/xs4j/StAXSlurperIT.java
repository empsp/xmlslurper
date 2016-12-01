package org.xs4j;

import org.xs4j.NodeNotifier.CollectData;
import org.xs4j.NodeNotifier.FindData;
import org.xs4j.listener.NodeListener;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.xs4j.XMLSlurperFactory.*;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Created by mturski on 11/28/2016.
 */
public class StAXSlurperIT {
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private XMLSlurper slurper = XMLSlurperFactory.getInstance().createXMLSlurper();

    @Test
    public void givenFindListenerOnInitialSlurpNodeSlurperShouldParseRootStartAndEndElements() throws Exception {
        // given
        NodeListener listener = mock(NodeListener.class);

        // when
        slurper.getNodes().find(listener);
        slurper.parse(getResource("simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");

        verify(listener, times(2)).onNode(null, root);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenStartNodeFindListenerOnInitialSlurpNodeSlurperShouldParseRootStartElementAndExit() throws Exception {
        // given
        NodeListener listener = mock(NodeListener.class);

        XMLStreamReader parser = createSlurperWithMockedXMLInputFactory();
        given(parser.hasNext()).willReturn(true, true, true, true, false);
        given(parser.next()).willReturn(START_ELEMENT, START_ELEMENT, END_ELEMENT, END_ELEMENT);
        given(parser.getEventType()).willReturn(START_ELEMENT, START_ELEMENT, END_ELEMENT, END_ELEMENT);
        given(parser.getAttributeCount()).willReturn(0);
        given(parser.getNamespaceCount()).willReturn(0);
        given(parser.getNamespaceURI()).willReturn(null);
        given(parser.getPrefix()).willReturn("");
        given(parser.getLocalName()).willReturn("ObjectTree", "Object", "Object", "ObjectTree");

        // when
        slurper.getNodes().find(listener, null);
        slurper.parse(getResource("simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");

        verify(listener).onNode(null, root);
        verifyNoMoreInteractions(listener);

        verify(parser).hasNext();
        verify(parser).next();
        verify(parser).getEventType();
        verify(parser).getNamespaceURI();
        verify(parser).getPrefix();
        verify(parser).getLocalName();
        verify(parser).getAttributeCount();
        verify(parser).getNamespaceCount();
        verify(parser).close();
        verifyNoMoreInteractions(parser);
    }

    @Test
    public void givenEndNodeFindListenerOnRootChildSlurpNodeSlurperShouldParseRootChildEndElementAndExit() throws Exception {
        // given
        NodeListener listener = mock(NodeListener.class);

        XMLStreamReader parser = createSlurperWithMockedXMLInputFactory();
        given(parser.hasNext()).willReturn(true, true, true, true, false);
        given(parser.next()).willReturn(START_ELEMENT, START_ELEMENT, END_ELEMENT, END_ELEMENT);
        given(parser.getEventType()).willReturn(START_ELEMENT, START_ELEMENT, END_ELEMENT, END_ELEMENT);
        given(parser.getAttributeCount()).willReturn(0);
        given(parser.getNamespaceCount()).willReturn(0);
        given(parser.getNamespaceURI()).willReturn(null);
        given(parser.getPrefix()).willReturn("");
        given(parser.getLocalName()).willReturn("ObjectTree", "Object", "Object", "ObjectTree");

        // when
        slurper.getNodes().node("ObjectTree").node("Object").find(null, listener);
        slurper.parse(getResource("simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");

        verify(listener).onNode(root, object);
        verifyNoMoreInteractions(listener);

        verify(parser, times(3)).hasNext();
        verify(parser, times(3)).next();
        verify(parser, times(3)).getEventType();
        verify(parser, times(2)).getNamespaceURI();
        verify(parser, times(2)).getPrefix();
        verify(parser, times(2)).getLocalName();
        verify(parser, times(2)).getAttributeCount();
        verify(parser, times(2)).getNamespaceCount();
        verify(parser).close();
        verifyNoMoreInteractions(parser);
    }

    private XMLNode createNode(long id, String name) {
        return nodeFactory.createNode(id, name, Collections.emptyMap());
    }

    private InputStream getResource(String resourceName) {
        return getClass().getResourceAsStream(resourceName);
    }

    private XMLStreamReader createSlurperWithMockedXMLInputFactory() throws XMLStreamException {
        List<FindData> findData = new ArrayList<FindData>();
        List<FindData> findAllData = new ArrayList<FindData>();
        List<CollectData> collectData = new ArrayList<CollectData>();

        XMLStreamReader parser = mock(XMLStreamReader.class);

        XMLInputFactory xmlInputFactory = mock(XMLInputFactory.class);
        given(xmlInputFactory.createXMLStreamReader(any(FileInputStream.class))).willReturn(parser);

        NodeFactory nodeFactory = getNodeFactory();
        slurper = new StAXSlurper(
                xmlInputFactory,
                nodeFactory,
                getSlurpFactory(findData, findAllData, collectData, getSlurpAlignmentFactory()),
                getNodeNotifier(findData, findAllData, collectData),
                getStAXNamespaceSensitiveElementParser(false, nodeFactory));

        return parser;
    }
}
