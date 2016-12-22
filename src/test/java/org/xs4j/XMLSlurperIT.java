package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.xs4j.listener.NodeListener;
import org.xs4j.path.Slurp;
import org.xs4j.path.SlurpNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.xs4j.TestUtil.*;

/**
 * Created by mturski on 11/10/2016.
 */
public class XMLSlurperIT {
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
        inOrder.verify(listener).onNode(root);
        inOrder.verify(listener, times(2)).onNode(firstObject);
        inOrder.verify(listener).onNode(root);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenRootSlurpNodeFindAllReturnsRoot() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("simpleTestCase.xml", "ObjectTree");

        // then
        verify(listener, times(2)).onNode(createNode(0L, "ObjectTree"));
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
        inOrder.verify(listener, times(2)).onNode(object);
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
        verify(listener, times(2)).onNode(nodeCaptor.capture());
        verifyNoMoreInteractions(listener);
        List<XMLNode> actualNodes = nodeCaptor.getAllValues();

        Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        XMLNode actualNode = actualNodes.get(0);
        assertThat(actualNode.getLocalName(), is("Object"));
        assertThat(actualNode.getText(), is("attrValue"));
        assertThat(actualNode.getAttributes(), is(expectedAttributes));
        assertThat(actualNode.getParent().getLocalName(), is("ObjectTree"));
    }

    @Test
    public void givenChildOfChildSlurpNodeFindAllReturnsNodesFromThatSpecificDepth() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", "ObjectTree", "Object", "OtherObject");

        // then
        XMLNode other = createNode(2L, "OtherObject");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(other);
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

        parser.parse(getResource(this, "borderTestCase.xml"));

        // then
        XMLNode object = createNode(1L, "Object");
        XMLNode other = createNode(2L, "OtherObject");

        InOrder inOrder = inOrder(objectListener, otherObjectListener);
        inOrder.verify(objectListener).onNode(object);
        inOrder.verify(otherObjectListener, times(2)).onNode(other);
        inOrder.verify(objectListener).onNode(object);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenStartNodeListenerFindAllReturnsStartNodeEventsWithPartialDataRead() throws Exception {
        // given
        final Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        listener = spy(new NodeListener() {
            @Override
            public void onNode(@NotNull XMLNode node) {
                assertThat(node.getLocalName(), is("Object"));
                assertNull(node.getText());
                assertThat(node.getAttributes(), is(expectedAttributes));
            }
        });

        // when
        getNodes().node("**").node("Object").findAll(listener, null);
        parser.parse(getResource(this, "simpleTestCase.xml"));

        // then
        verify(listener).onNode(any(XMLNode.class));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenEndNodeListenerFindAllReturnsEndNodeEventsWithAllDataRead() throws Exception {
        // given
        final Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        listener = spy(new NodeListener() {
            @Override
            public void onNode(@NotNull XMLNode node) {
                assertThat(node.getLocalName(), is("Object"));
                assertThat(node.getText(), is("attrValue"));
                assertThat(node.getAttributes(), is(expectedAttributes));
            }
        });

        // when
        getNodes().node("**").node("Object").findAll(null, listener);
        parser.parse(getResource(this, "simpleTestCase.xml"));

        // then
        verify(listener).onNode(any(XMLNode.class));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeOnInitialSlurpNodeFindAllReturnsAllNodesWithThatAttribute() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", getNodes().attr("attr1"));

        // then
        XMLNode object = createNode(1L, "Object");
        XMLNode otherObject = createNode(2L, "OtherObject");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).onNode(object);
        inOrder.verify(listener, times(2)).onNode(otherObject);
        inOrder.verify(listener).onNode(object);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpAttributeForChildFindAllReturnsChildNodesWithThatAttribute() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("borderTestCase.xml", getNodes("ObjectTree", "Object").attr("attr2"));

        // then
        XMLNode object = createNode(1L, "Object");

        verify(listener, times(2)).onNode(object);
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
        XMLNode sibling1st = createNode(1L, "Car");
        XMLNode sibling2nd = createNode(5L, "Plane");

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(sibling1st);
        inOrder.verify(listener, times(2)).onNode(sibling2nd);
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
        XMLNode object = createNode(1L, "Car");

        verify(listener, times(2)).onNode(object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeValueFindAllReturnsImmediateChildrenOfRootWithThatAttributeWithThatValue() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", getNodes("Transport", "*").attr("manufacturer").is("Boeing"));

        // then
        XMLNode object = createNode(5L, "Plane");

        verify(listener, times(2)).onNode(object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithExcludedValueFindAllReturnsAllOfThatLevelExceptOneHavingThatAttributeWithThatValue() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        parse("siblingsTestCase.xml", getNodes("Transport", "*", "Engine").attr("type").isNot("Turbofan"));

        // then
        XMLNode child = createNode(2L, "Engine");

        verify(listener, times(2)).onNode(child);
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
        XMLNode child = createNode(4L, "OtherObject");

        verify(listener, times(2)).onNode(child);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpNodeNamespaceAwareFindAllReturnsNodeNamespaceComplaint() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        getNodes("**", "other:OtherObject", "OtherObject").attr("other:attr2").findAll(listener, null);
        parser.parse(getResource(this, "namespaceTestCase.xml"));

        // then
        Map<String, String> parentObjectAttrs = new HashMap<String, String>();
        parentObjectAttrs.put("xmlns:other", "http://other");
        parentObjectAttrs.put("other:attr3", "123");
        XMLNode parentObject = createNode(3L, "http://other", "other", "OtherObject", parentObjectAttrs);

        Map<String, String> otherObjectAttrs = new HashMap<String, String>();
        otherObjectAttrs.put("xmlns:other", "http://other");
        otherObjectAttrs.put("other:attr2", "OTHER = 123,OTHER_OBJECT = 125");
        XMLNode otherObject = createNode(5L, "http://general", null, "OtherObject", otherObjectAttrs);

        ArgumentCaptor<XMLNode> nodeCaptor = ArgumentCaptor.forClass(XMLNode.class);
        verify(listener).onNode(nodeCaptor.capture());
        verifyNoMoreInteractions(listener);

        List<XMLNode> actualNodes = nodeCaptor.getAllValues();
        XMLNode actualOtherObject = actualNodes.get(0);

        assertThat(actualOtherObject.getId(), is(otherObject.getId()));
        assertThat(actualOtherObject.getNamespace(), is(otherObject.getNamespace()));
        assertThat(actualOtherObject.getPrefix(), is(otherObject.getPrefix()));
        assertThat(actualOtherObject.getLocalName(), is(otherObject.getLocalName()));
        assertThat(actualOtherObject.getQName(), is(otherObject.getQName()));
        assertThat(actualOtherObject.getAttributes(), is(otherObject.getAttributes()));

        XMLNode actualParentObject = actualOtherObject.getParent();

        assertThat(actualParentObject.getId(), is(parentObject.getId()));
        assertThat(actualParentObject.getNamespace(), is(parentObject.getNamespace()));
        assertThat(actualParentObject.getPrefix(), is(parentObject.getPrefix()));
        assertThat(actualParentObject.getLocalName(), is(parentObject.getLocalName()));
        assertThat(actualParentObject.getQName(), is(parentObject.getQName()));
        assertThat(actualParentObject.getAttributes(), is(parentObject.getAttributes()));
    }

    @Test
    public void givenSchemaIgnorableCharactersAreNotPartOfTheText() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        getNodes("ObjectTree", "Object").findAll(null, listener);
        parser.parse(getResource(this, "borderTestCase.xml"), getResourceAsFile(this, "borderTestCaseSchema.xsd"));

        // then
        ArgumentCaptor<XMLNode> nodeCaptor = ArgumentCaptor.forClass(XMLNode.class);
        verify(listener).onNode(nodeCaptor.capture());
        verifyNoMoreInteractions(listener);

        List<XMLNode> actualNodes = nodeCaptor.getAllValues();
        XMLNode actualOtherObject = actualNodes.get(0);

        assertNull(actualOtherObject.getText());
    }

    @Test
    public void givenSchemaSignificantCharactersArePartOfTheText() throws Exception {
        // given
        listener = mock(NodeListener.class);

        // when
        getNodes("ObjectTree", "OtherObject").findAll(null, listener);
        parser.parse(getResource(this, "borderTestCase.xml"), getResourceAsFile(this, "borderTestCaseSchema.xsd"));

        // then
        ArgumentCaptor<XMLNode> nodeCaptor = ArgumentCaptor.forClass(XMLNode.class);
        verify(listener).onNode(nodeCaptor.capture());
        verifyNoMoreInteractions(listener);

        List<XMLNode> actualNodes = nodeCaptor.getAllValues();
        XMLNode actualOtherObject = actualNodes.get(0);

        assertThat(actualOtherObject.getText(), is("\n        \n        \n    "));
    }

    @After
    public void teardown() {
        listener = null;
    }

    private SlurpNode getNodes(String... nodePath) {
        return parser.getNodes(nodePath);
    }

    private void parse(String resourcePath, String... nodePath) throws Exception {
        parse(resourcePath, getNodes(nodePath));
    }

    private void parse(String resourcePath, Slurp slurp) throws Exception {
        slurp.findAll(listener);

        parser.parse(getResource(this, resourcePath));
    }
}
