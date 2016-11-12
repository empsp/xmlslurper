package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.event.SlurpListener;
import com.tsolutions.xmlslurper.path.Node;
import com.tsolutions.xmlslurper.path.SlurpNode;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by mturski on 11/10/2016.
 */
public class StAXSlurperTest {
    private static final NodeFactory nodeFactory = StAXSlurperFactory.getStAXNodeFactory();

    private XMLSlurper slurper = StAXSlurperFactory.getInstance().createXMLSlurper();

    @Test
    public void givenInitialSlurpNodeFindAllReturnsAllNodesInOrder() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("simpleTestCase.xml", listener);

        // then
        Node root = nodeFactory.createNode(0L, "ObjectTree", emptyMap());
        Node firstObject = nodeFactory.createNode(1L, "Object", emptyMap());

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onNode(null, root);
        inOrder.verify(listener, times(2)).onNode(root, firstObject);
        inOrder.verify(listener).onNode(null, root);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeWithRootNodeSetFindAllReturnsRootNodeOnly() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("simpleTestCase.xml", listener, "ObjectTree");

        // then
        verify(listener, times(2)).onNode(null, nodeFactory.createNode(0L, "ObjectTree", emptyMap()));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpNodeWithChildNodeSetFindAllReturnsOnlyThatChild() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("simpleTestCase.xml", listener, "ObjectTree", "Object");

        // then
        Node root = nodeFactory.createNode(0L, "ObjectTree", emptyMap());
        Node object = nodeFactory.createNode(1L, "Object", emptyMap());

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(root, object);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenXmlFileSlurpNodeFindAllReturnsCompleteData() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("simpleTestCase.xml", listener, "ObjectTree", "Object");

        // then
        ArgumentCaptor<Node> nodeCaptor = ArgumentCaptor.forClass(Node.class);
        verify(listener, times(2)).onNode(nodeCaptor.capture(), nodeCaptor.capture());
        List<Node> actualNodes = nodeCaptor.getAllValues();

        Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        Node actualObject = actualNodes.get(1);
        assertThat(actualObject.getName(), is("Object"));
        assertThat(actualObject.getText(), is("attrValue"));
        assertThat(actualObject.getAttributes(), is(expectedAttributes));
    }

    @Test
    public void givenSpecificDepthOfNodeFindAllReturnsNodesFromThatSpecificDepthOnly() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("borderTestCase1.xml", listener, "ObjectTree", "Object", "OtherObject");

        // then
        Node object = nodeFactory.createNode(1L, "Object", emptyMap());
        Node other = nodeFactory.createNode(2L, "OtherObject", emptyMap());
        Node object2nd = nodeFactory.createNode(4L, "Object", emptyMap());
        Node other2nd = nodeFactory.createNode(5L, "OtherObject", emptyMap());

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(object, other);
        inOrder.verify(listener, times(2)).onNode(object2nd, other2nd);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeSetForChildNodeButOmittedRootNodeFindAllReturnsNothing() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("borderTestCase1.xml", listener, "Object");

        // then
        verifyNoMoreInteractions(listener);
    }

    private String getResourcePath(String resourceName) {
        return getClass().getResource(resourceName).getPath();
    }

    private void parse(String resourcePath, SlurpListener listener, String... nodePath) throws IOException, XMLStreamException {
        SlurpNode slurpNode = slurper.parse(getResourcePath(resourcePath));

        for(String nodeName : nodePath)
            slurpNode = slurpNode.node(nodeName);

        slurpNode.find(listener);
    }
}
