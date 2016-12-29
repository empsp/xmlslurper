package org.xs4j;

import org.junit.After;
import org.junit.Test;
import org.xs4j.listener.NodeListener;

import javax.xml.stream.XMLStreamException;

import static org.mockito.Mockito.*;
import static org.xs4j.TestUtil.createNode;
import static org.xs4j.TestUtil.getResource;
import static org.xs4j.XMLSlurperFactory.*;

/**
 * Created by mturski on 12/5/2016.
 */
public class SAXSlurperIT {
    private XMLSlurper slurper;

    @Test
    public void givenFindListenerOnRootNodeSlurperShouldParseStartAndEndElementOnRootNode() throws Exception {
        // given
        slurper = XMLSlurperFactory.getInstance().createXMLSlurper();
        NodeListener listener = mock(NodeListener.class);

        // when
        slurper.getNodes().find(listener);
        slurper.parse(getResource(this, "simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");

        verify(listener, times(2)).onNode(root);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenFindListenerOnRootStartNodeSlurperShouldParseRootStartElementAndExit() throws Exception {
        // given
        NodeNotifier nodeNotifier = createSlurperWithSpyNodeNotifier();
        NodeListener listener = mock(NodeListener.class);

        // when
        slurper.getNodes().find(listener, null);
        slurper.parse(getResource(this, "simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");

        verify(listener).onNode(root);
        verifyNoMoreInteractions(listener);

        verify(nodeNotifier, never()).peekLastDescendant();
        verify(nodeNotifier, never()).onEndNode();
        verify(nodeNotifier).onStartNode(root);
    }

    @Test
    public void givenFindListenerOnChildEndNodeSlurperShouldParseTillChildEndElementInclusiveAndExit() throws Exception {
        // given
        NodeNotifier nodeNotifier = createSlurperWithSpyNodeNotifier();
        NodeListener listener = mock(NodeListener.class);

        // when
        slurper.getNodes().node("ObjectTree").node("Object").find(null, listener);
        slurper.parse(getResource(this, "simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");

        verify(listener).onNode(object);
        verifyNoMoreInteractions(listener);

        verify(nodeNotifier, times(2)).peekLastDescendant();
        verify(nodeNotifier).onEndNode();
        verify(nodeNotifier).onStartNode(root);
        verify(nodeNotifier).onStartNode(object);
    }

    @After
    public void teardown() {
        slurper = null;
    }

    private NodeNotifier createSlurperWithSpyNodeNotifier() throws XMLStreamException {
        NodeNotifier nodeNotifier = spy(getNodeNotifier());

        XMLNodeFactory xmlNodeFactory = XMLNodeFactory.getInstance();
        slurper = new SAXSlurper(
                getSaxParserFactory(true),
                getSchemaFactory(),
                getSlurpFactory(nodeNotifier, getSlurpAlignmentFactory()),
                nodeNotifier,
                getSAXNamespaceSensitiveElementParser(false, xmlNodeFactory));

        return nodeNotifier;
    }
}
