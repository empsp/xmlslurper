package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xs4j.listener.NodeListener;
import org.xs4j.path.Slurp;
import org.xs4j.path.SlurpNode;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.io.InputStream;
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
public class XMLSlurperIT {
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
    public void givenRootSlurpNodeFindAllReturnsRoot() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("simpleTestCase.xml", "ObjectTree");

        // then
        verify(listener, times(2)).onNode(null, createNode(0L, "ObjectTree"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenChildSlurpNodeFindAllReturnsChildOfRoot() throws Exception {
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
    public void givenChildSlurpNodeFindAllReturnsCompleteDataForChildOfRoot() throws Exception {
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
    public void givenChildOfChildSlurpNodeFindAllReturnsNodesFromThatSpecificDepth() throws Exception {
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
    public void givenDifferentRootSlurpNodeFindAllReturnsNothing() throws Exception {
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

        parser.parse(getResource("borderTestCase.xml"));

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
        parser.parse(getResource("simpleTestCase.xml"));

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
        parser.parse(getResource("simpleTestCase.xml"));

        // then
        verify(listener).onNode(any(), any());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeOnInitialSlurpNodeFindAllReturnsAllNodesWithThatAttribute() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", getNodes().attr("attr1"));

        // then
        XMLNode root = createNode(0L, "ObjectTree");
        XMLNode object = createNode(1L, "Object");
        XMLNode otherObject = createNode(2L, "OtherObject");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onNode(root, object);
        inOrder.verify(listener, times(2)).onNode(object, otherObject);
        inOrder.verify(listener).onNode(root, object);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpAttributeForChildFindAllReturnsChildNodesWithThatAttribute() throws Exception {
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
    public void givenSlurpAttributeForNonExistentAttributeFindAllReturnsNothing() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", getNodes("ObjectTree", "Object").attr("attr3"));

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSiblingsSlurpNodeFindAllReturnsAllImmediateChildrenOfRoot() throws Exception {
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
    public void givenSiblingSlurpNodeWithoutRootFindAllReturnsNothing() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", "Car", "*");

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeForSiblingsFindAllReturnsImmediateChildrenOfRootWithThatAttribute() throws Exception {
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
    public void givenSlurpAttributeValueFindAllReturnsImmediateChildrenOfRootWithThatAttributeWithThatValue() throws Exception {
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
    public void givenSlurpAttributeWithExcludedValueFindAllReturnsAllOfThatLevelExceptOneHavingThatAttributeWithThatValue() throws Exception {
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

    @Test
    public void givenSlurpAttributeWithArrayOfAllPossibleValuesToBeExcludedFindAllReturnsNothing() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", getNodes("Transport", "*", "Engine").attr("type").isNot("Hybrid Electric-Gasoline", "Turbofan"));

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithRegexFindAllReturnsNodeWithAttributeWithValueMatching() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", getNodes().attr("attr2").regex("[^,]?OTHER[\\w_]*[\\s=]+\\d{2}4"));

        // then
        XMLNode object = createNode(3L, "OtherObject");
        XMLNode child = createNode(4L, "OtherObject");

        verify(listener, times(2)).onNode(object, child);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpNodeNamespaceAwareFindAllReturnsNodeNamespaceComplaint() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        getNodes("**", "other:OtherObject").findAll(listener, null);
        parser.parse(getResource("namespaceTestCase.xml"));

        // then
        Map<String, String> otherObjectAttrs = new HashMap<String, String>();
        otherObjectAttrs.put("xmlns:other", "http://other");
        otherObjectAttrs.put("other:attr3", "123");
        XMLNode otherObject = createNode(3L, "http://other", "other", "OtherObject", otherObjectAttrs);

        ArgumentCaptor<XMLNode> nodeCaptor = ArgumentCaptor.forClass(XMLNode.class);
        verify(listener).onNode(nodeCaptor.capture(), nodeCaptor.capture());
        verifyNoMoreInteractions(listener);

        List<XMLNode> actualNodes = nodeCaptor.getAllValues();
        XMLNode actualOtherObject = actualNodes.get(1);

        assertThat(actualOtherObject.getNamespace(), is(otherObject.getNamespace()));
        assertThat(actualOtherObject.getPrefix(), is(otherObject.getPrefix()));
        assertThat(actualOtherObject.getLocalName(), is(otherObject.getLocalName()));
        assertThat(actualOtherObject.getQName(), CoreMatchers.is(otherObject.getPrefix() + NodeFactory.QNAME_SEPARATOR + otherObject.getLocalName()));
        assertThat(actualOtherObject.getAttributes(), is(otherObject.getAttributes()));
    }

    @After
    public void teardown() {
        listener = null;
    }

    private InputStream getResource(String resourceName) {
        return getClass().getResourceAsStream(resourceName);
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

        parser.parse(getResource(resourcePath));
    }

    private XMLNode createNode(long id, String localName) {
        return nodeFactory.createNode(id, localName, Collections.emptyMap());
    }

    private XMLNode createNode(long id, String namespace, String prefix, String localName, Map<String, String> attributeByQName) {
        return nodeFactory.createNode(id, namespace, prefix, localName, attributeByQName);
    }
}
