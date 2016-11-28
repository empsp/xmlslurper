package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.listener.NodeListener;
import org.junit.Before;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tsolutions.xmlslurper.XMLSlurperFactory.*;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Created by mturski on 11/28/2016.
 */
public class StAXSlurperIT {
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private XMLSlurper slurper;

    @Before
    public void setup() {
        slurper = XMLSlurperFactory.getInstance().createXMLSlurper(ParserType.STAX_PARSER);
    }

    @Test
    public void givenFindListenerOnInitialSlurpNodeSlurperShouldParseRootStartAndEndElements() throws Exception {
        // given
        NodeListener listener = mock(NodeListener.class);

        // when
        slurper.getNodes().find(listener);
        slurper.parse(getResourcePath("simpleTestCase.xml"));

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
        given(parser.getLocalName()).willReturn("ObjectTree", "Object", "Object", "ObjectTree");

        // when
        slurper.getNodes().find(listener, null);
        slurper.parse(getResourcePath("simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");

        verify(listener).onNode(null, root);
        verifyNoMoreInteractions(listener);

        verify(parser).hasNext();
        verify(parser).next();
        verify(parser).getEventType();
        verify(parser).getLocalName();
        verify(parser).getAttributeCount();
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
        given(parser.getLocalName()).willReturn("ObjectTree", "Object", "Object", "ObjectTree");

        // when
        slurper.getNodes().node("ObjectTree").node("Object").find(null, listener);
        slurper.parse(getResourcePath("simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");

        verify(listener).onNode(root, object);
        verifyNoMoreInteractions(listener);

        verify(parser, times(3)).hasNext();
        verify(parser, times(3)).next();
        verify(parser, times(3)).getEventType();
        verify(parser, times(2)).getLocalName();
        verify(parser, times(2)).getAttributeCount();
        verify(parser).close();
        verifyNoMoreInteractions(parser);
    }

    private XMLNode createNode(long id, String name) {
        return nodeFactory.createNode(id, name, Collections.emptyMap());
    }

    private String getResourcePath(String resourceName) {
        return getClass().getResource(resourceName).getPath();
    }

    private XMLStreamReader createSlurperWithMockedXMLInputFactory() throws XMLStreamException {
        List<NodeNotifier.NodeNotifierData> findData = new ArrayList<NodeNotifier.NodeNotifierData>();
        List<NodeNotifier.NodeNotifierData> findAllData = new ArrayList<NodeNotifier.NodeNotifierData>();

        XMLStreamReader parser = mock(XMLStreamReader.class);

        XMLInputFactory xmlInputFactory = mock(XMLInputFactory.class);
        given(xmlInputFactory.createXMLStreamReader(any(FileInputStream.class))).willReturn(parser);

        slurper = new StAXSlurper(
                xmlInputFactory,
                getNodeFactory(),
                getSlurpFactory(findData, findAllData, getSlurpAlignmentFactory()),
                getNodeNotifier(findData, findAllData));

        return parser;
    }
}
