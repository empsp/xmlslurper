package org.xs4j;

import org.junit.After;
import org.junit.Test;
import org.xs4j.NodeNotifier.FindData;
import org.xs4j.listener.NodeListener;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.xs4j.XMLSlurperFactory.*;

/**
 * Created by mturski on 12/5/2016.
 */
public class SAXSlurperIT {
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private XMLSlurper slurper;

    @Test
    public void givenFindListenerOnRootNodeSlurperShouldParseStartAndEndElementOnRootNode() throws Exception {
        // given
        slurper = XMLSlurperFactory.getInstance().createXMLSlurper();
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
    public void givenFindListenerOnRootStartNodeSlurperShouldParseRootStartElementAndExit() throws Exception {
        // given
        NodeNotifier nodeNotifier = createSlurperWithSpyNodeNotifier();
        NodeListener listener = mock(NodeListener.class);

        // when
        slurper.getNodes().find(listener, null);
        slurper.parse(getResource("simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");

        verify(listener).onNode(null, root);
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
        slurper.parse(getResource("simpleTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");

        verify(listener).onNode(root, object);
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

    private XMLNode createNode(long id, String name) {
        return nodeFactory.createNode(id, name, Collections.<String, String> emptyMap());
    }

    private InputStream getResource(String resourceName) {
        return getClass().getResourceAsStream(resourceName);
    }

    private NodeNotifier createSlurperWithSpyNodeNotifier() throws XMLStreamException {
        List<FindData> findData = new ArrayList<FindData>();
        List<FindData> findAllData = new ArrayList<FindData>();

        NodeNotifier nodeNotifier = spy(getNodeNotifier(findData, findAllData));

        NodeFactory nodeFactory = getNodeFactory();
        slurper = new SAXSlurper(
                getSaxParserFactory(true),
                nodeFactory,
                getSlurpFactory(findData, findAllData, getSlurpAlignmentFactory()),
                nodeNotifier,
                getSAXNamespaceSensitiveElementParser(false, nodeFactory));

        return nodeNotifier;
    }
}
