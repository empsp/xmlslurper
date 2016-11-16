package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.SlurpNode;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

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
public class XMLSlurperTest {
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private XMLSlurper parser = XMLSlurperFactory.getInstance().createXMLSlurper();

    @Test
    public void givenInitialSlurpNodeFindAllReturnsAllNodesInOrder() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("simpleTestCase.xml", listener);

        // then
        XMLNode root = nodeFactory.createNode(0L, "ObjectTree", emptyMap());
        XMLNode firstObject = nodeFactory.createNode(1L, "Object", emptyMap());

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
        XMLNode root = nodeFactory.createNode(0L, "ObjectTree", emptyMap());
        XMLNode object = nodeFactory.createNode(1L, "Object", emptyMap());

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
        ArgumentCaptor<XMLNode> nodeCaptor = ArgumentCaptor.forClass(XMLNode.class);
        verify(listener, times(2)).onNode(nodeCaptor.capture(), nodeCaptor.capture());
        List<XMLNode> actualNodes = nodeCaptor.getAllValues();

        Map<String, String> expectedAttributes = new HashMap<String, String>();
        expectedAttributes.put("attr1", "SOMEOBJ");
        expectedAttributes.put("attr2", "E=1");

        XMLNode actualObject = actualNodes.get(1);
        assertThat(actualObject.getName(), is("Object"));
        assertThat(actualObject.getText(), is("attrValue"));
        assertThat(actualObject.getAttributes(), is(expectedAttributes));
    }

    @Test
    public void givenSpecificDepthOfNodeFindAllReturnsNodesFromThatSpecificDepthOnly() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("borderTestCase.xml", listener, "ObjectTree", "Object", "OtherObject");

        // then
        XMLNode object = nodeFactory.createNode(1L, "Object", emptyMap());
        XMLNode other = nodeFactory.createNode(2L, "OtherObject", emptyMap());

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(object, other);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeSetForChildNodeButOmittedRootNodeFindAllReturnsNothing() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("borderTestCase.xml", listener, "Object");

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenTwoListenersSetOnDifferentSlurpNodesFindAllReturnsRelevantNodesForBothListeners() throws Exception {
        // given
        SlurpListener objectListener = mock(SlurpListener.class);
        SlurpListener otherObjectListener = mock(SlurpListener.class);

        // when
        SlurpNode objectSlurpNode = parser.getNodes().node("ObjectTree").node("Object");
        objectSlurpNode.findAll(objectListener);
        objectSlurpNode.node("OtherObject").findAll(otherObjectListener);

        parser.parse(getResourcePath("borderTestCase.xml"));

        // then
        XMLNode root = nodeFactory.createNode(0L, "ObjectTree", emptyMap());
        XMLNode object = nodeFactory.createNode(1L, "Object", emptyMap());
        XMLNode other = nodeFactory.createNode(2L, "OtherObject", emptyMap());

        InOrder inOrder = inOrder(objectListener, otherObjectListener);
        inOrder.verify(objectListener).onNode(root, object);
        inOrder.verify(otherObjectListener, times(2)).onNode(object, other);
        inOrder.verify(objectListener).onNode(root, object);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpAttributeSetFindAllReturnsNodesWithRelevantAttributeSet() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parser.getNodes().node("ObjectTree").node("Object").attr("attr2").findAll(listener);
        parser.parse(getResourcePath("borderTestCase.xml"));

        // then
        XMLNode root = nodeFactory.createNode(0L, "ObjectTree", emptyMap());
        XMLNode object = nodeFactory.createNode(1L, "Object", emptyMap());

        verify(listener, times(2)).onNode(root, object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeSetForNonExistentAttributeFindAllReturnsNothing() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parser.getNodes().node("ObjectTree").node("Object").attr("attr3").findAll(listener);
        parser.parse(getResourcePath("borderTestCase.xml"));

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpNodeSetForSiblingsOfRootNodeFindAllReturnsAllSiblingsForThatDepthLevel() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("siblingsTestCase.xml", listener, "Transport", "*");

        // then
        XMLNode root = nodeFactory.createNode(0L, "Transport", emptyMap());
        XMLNode sibling1st = nodeFactory.createNode(1L, "Car", emptyMap());
        XMLNode sibling2nd = nodeFactory.createNode(5L, "Plane", emptyMap());

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(root, sibling1st);
        inOrder.verify(listener, times(2)).onNode(root, sibling2nd);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeSetForSiblingsOfChildNodeButOmittedRootNodeFindAllReturnsNothing() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("siblingsTestCase.xml", listener, "Car", "*");

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithNodeSetForSiblingsOfRootNodeFindAllReturnsRelevantNodes() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parser.getNodes().node("Transport").node("*").attr("modelVersion").findAll(listener);
        parser.parse(getResourcePath("siblingsTestCase.xml"));

        // then
        XMLNode root = nodeFactory.createNode(0L, "Transport", emptyMap());
        XMLNode object = nodeFactory.createNode(1L, "Car", emptyMap());

        verify(listener, times(2)).onNode(root, object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithValueSetAndNodeSetForSiblingsOfRootNodeFindAllReturnsRelevantNodes() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parser.getNodes().node("Transport").node("*").attr("manufacturer").is("Boeing").findAll(listener);
        parser.parse(getResourcePath("siblingsTestCase.xml"));

        // then
        XMLNode root = nodeFactory.createNode(0L, "Transport", emptyMap());
        XMLNode object = nodeFactory.createNode(5L, "Plane", emptyMap());

        verify(listener, times(2)).onNode(root, object);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpAttributeWithExcludedValueSetAndNodeSetForChildNodeOfSiblingsOfRootNodeFindAllReturnsRelevantNodes() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parser.getNodes().node("Transport").node("*").node("Engine").attr("type").isNot("Turbofan").findAll(listener);
        parser.parse(getResourcePath("siblingsTestCase.xml"));

        // then
        XMLNode object = nodeFactory.createNode(1L, "Car", emptyMap());
        XMLNode child = nodeFactory.createNode(2L, "Engine", emptyMap());

        verify(listener, times(2)).onNode(object, child);
        verifyNoMoreInteractions(listener);
    }

    private String getResourcePath(String resourceName) {
        return getClass().getResource(resourceName).getPath();
    }

    private void parse(String resourcePath, SlurpListener listener, String... nodePath) throws Exception {
        SlurpNode slurpNode = parser.getNodes();

        for(String nodeName : nodePath)
            slurpNode = slurpNode.node(nodeName);

        slurpNode.findAll(listener);

        parser.parse(getResourcePath(resourcePath));
    }
}
