package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.tsolutions.xmlslurper.listener.NodeListener;
import com.tsolutions.xmlslurper.path.Slurp;
import com.tsolutions.xmlslurper.path.SlurpNode;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by mturski on 11/10/2016.
 */
public class XMLSlurperTest {
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private XMLSlurper parser = XMLSlurperFactory.getInstance().createXMLSlurper();
    private NodeListener listener;

    @Test
    public void givenInitialSlurpNodeFindAllReturnsAllNodesInOrder() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("simpleTestCase.xml");

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode firstObject = createNode(1L, "Object");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onNode(null, root);
        inOrder.verify(listener, times(2)).onNode(root, firstObject);
        inOrder.verify(listener).onNode(null, root);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeWithRootNodeSetFindAllReturnsRootNode() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("simpleTestCase.xml", "ObjectTree");

        // then
        verify(listener, times(2)).onNode(null, createNode(0L, "ObjectTree"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpNodeWithChildNodeSetFindAllReturnsTheChild() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("simpleTestCase.xml", "ObjectTree", "Object");

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(root, object);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenXmlFileSlurpNodeFindAllReturnsCompleteData() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("simpleTestCase.xml", "ObjectTree", "Object");

        // then
        ArgumentCaptor<XMLNode> nodeCaptor = ArgumentCaptor.forClass(XMLNode.class);
        verify(listener, times(2)).onNode(nodeCaptor.capture(), nodeCaptor.capture());
        verifyNoMoreInteractions(listener);
        List<XMLNode> actualNodes = nodeCaptor.getAllValues();

        Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        XMLNode actualNode = actualNodes.get(1);
        assertThat(actualNode.getLocalName(), is("Object"));
        assertThat(actualNode.getText(), is("attrValue"));
        assertThat(actualNode.getAttributes(), is(expectedAttributes));
    }

    @Test
    public void givenSpecificDepthOfNodeFindAllReturnsNodesFromThatSpecificDepth() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", "ObjectTree", "Object", "OtherObject");

        // then
        XMLNode object = createNode(1L, "Object");
        XMLNode other = createNode(2L, "OtherObject");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(object, other);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeSetForChildNodeButOmittedRootNodeFindAllReturnsNothing() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", "Object");

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenTwoListenersSetOnDifferentSlurpNodesFindAllReturnsRelevantNodesForBothListeners() throws Exception {
        // given
        NodeListener objectListener = mock(NodeListener.class);
        NodeListener otherObjectListener = mock(NodeListener.class);

        // when
        SlurpNode objectSlurpNode = parser.getNodes().node("ObjectTree").node("Object");
        objectSlurpNode.findAll(objectListener);
        objectSlurpNode.node("OtherObject").findAll(otherObjectListener);

        parser.parse(getResourcePath("borderTestCase.xml"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");
        XMLNode other = createNode(2L, "OtherObject");

        InOrder inOrder = inOrder(objectListener, otherObjectListener);
        inOrder.verify(objectListener).onNode(root, object);
        inOrder.verify(otherObjectListener, times(2)).onNode(object, other);
        inOrder.verify(objectListener).onNode(root, object);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenStartNodeListenerFindAllReturnsStartNodeEventsWithPartialDataRead() throws Exception {
        // given
        Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        listener = spy(new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                assertThat(node.getLocalName(), is("Object"));
                assertNull(node.getText());
                assertThat(node.getAttributes(), is(expectedAttributes));
            }
        });

        // when
        getNodes().node("**").node("Object").findAll(listener, null);
        parser.parse(getResourcePath("simpleTestCase.xml"));


        // then
        verify(listener).onNode(any(), any());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenEndNodeListenerFindAllReturnsEndNodeEventsWithAllDataRead() throws Exception {
        // given
        Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        listener = spy(new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                assertThat(node.getLocalName(), is("Object"));
                assertThat(node.getText(), is("attrValue"));
                assertThat(node.getAttributes(), is(expectedAttributes));
            }
        });

        // when
        getNodes().node("**").node("Object").findAll(null, listener);
        parser.parse(getResourcePath("simpleTestCase.xml"));

        // then
        verify(listener).onNode(any(), any());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeSetFindAllReturnsNodesWithRelevantAttributeSet() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", getNodes("ObjectTree", "Object").attr("attr2"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");

        verify(listener, times(2)).onNode(root, object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeSetForNonExistentAttributeFindAllReturnsNothing() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", getNodes("ObjectTree", "Object").attr("attr3"));

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpNodeSetForSiblingsOfRootNodeFindAllReturnsAllSiblingsForThatDepthLevel() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", "Transport", "*");

        // then
        XMLNode root = createNode(0L, "Transport");
        XMLNode sibling1st = createNode(1L, "Car");
        XMLNode sibling2nd = createNode(5L, "Plane");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(root, sibling1st);
        inOrder.verify(listener, times(2)).onNode(root, sibling2nd);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeSetForSiblingsOfChildNodeButOmittedRootNodeFindAllReturnsNothing() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", "Car", "*");

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithNodeSetForSiblingsOfRootNodeFindAllReturnsRelevantNodes() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", getNodes("Transport", "*").attr("modelVersion"));

        // then
        XMLNode root = createNode(0L, "Transport");
        XMLNode object = createNode(1L, "Car");

        verify(listener, times(2)).onNode(root, object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithValueSetAndNodeSetForSiblingsOfRootNodeFindAllReturnsRelevantNodes() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", getNodes("Transport", "*").attr("manufacturer").is("Boeing"));

        // then
        XMLNode root = createNode(0L, "Transport");
        XMLNode object = createNode(5L, "Plane");

        verify(listener, times(2)).onNode(root, object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithExcludedValueSetAndNodeSetForChildNodeOfSiblingsOfRootNodeFindAllReturnsRelevantNodes() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", getNodes("Transport", "*", "Engine").attr("type").isNot("Turbofan"));

        // then
        XMLNode object = createNode(1L, "Car");
        XMLNode child = createNode(2L, "Engine");

        verify(listener, times(2)).onNode(object, child);
        verifyNoMoreInteractions(listener);
    }

    @After
    public void teardown() {
        listener = null;
    }

    private String getResourcePath(String resourceName) {
        return getClass().getResource(resourceName).getPath();
    }

    private SlurpNode getNodes(String... nodePath) {
        SlurpNode slurpNode = parser.getNodes();

        for(String nodeName : nodePath)
            slurpNode = slurpNode.node(nodeName);

        return slurpNode;
    }

    private void parse(String resourcePath, String... nodePath) throws Exception {
        parse(resourcePath, getNodes(nodePath));
    }

    private void parse(String resourcePath, Slurp slurp) throws Exception {
        slurp.findAll(listener);

        parser.parse(getResourcePath(resourcePath));
    }

    private XMLNode createNode(long id, String name) {
        return nodeFactory.createNode(id, name, Collections.emptyMap());
    }
}
