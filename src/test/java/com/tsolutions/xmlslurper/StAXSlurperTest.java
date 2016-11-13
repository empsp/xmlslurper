package com.tsolutions.xmlslurper;

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
    public void givenInitialSlurpNodeFindReturnsAllNodesInOrder() throws Exception {
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
    public void givenSlurpNodeWithRootNodeSetFindReturnsRootNodeOnly() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("simpleTestCase.xml", listener, "ObjectTree");

        // then
        verify(listener, times(2)).onNode(null, nodeFactory.createNode(0L, "ObjectTree", emptyMap()));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenSlurpNodeWithChildNodeSetFindReturnsOnlyThatChild() throws Exception {
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
    public void givenXmlFileSlurpNodeFindReturnsCompleteData() throws Exception {
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
    public void givenSpecificDepthOfNodeFindReturnsNodesFromThatSpecificDepthOnly() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("borderTestCase1.xml", listener, "ObjectTree", "Object", "OtherObject");

        // then
        XMLNode object = nodeFactory.createNode(1L, "Object", emptyMap());
        XMLNode other = nodeFactory.createNode(2L, "OtherObject", emptyMap());
        XMLNode object2nd = nodeFactory.createNode(4L, "Object", emptyMap());
        XMLNode other2nd = nodeFactory.createNode(5L, "OtherObject", emptyMap());

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, times(2)).onNode(object, other);
        inOrder.verify(listener, times(2)).onNode(object2nd, other2nd);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void givenSlurpNodeSetForChildNodeButOmittedRootNodeFindReturnsNothing() throws Exception {
        // given
        SlurpListener listener = mock(SlurpListener.class);

        // when
        parse("borderTestCase1.xml", listener, "Object");

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void givenTwoListenersSetOnDifferentSlurpNodesFindReturnsRelevantNodesForBothListeners() throws Exception {
        // given
        SlurpListener objectListener = mock(SlurpListener.class);
        SlurpListener otherObjectListener = mock(SlurpListener.class);

        // when
        SlurpNode objectSlurpNode = slurper.getNodes().node("ObjectTree").node("Object");
        objectSlurpNode.find(objectListener);
        objectSlurpNode.node("OtherObject").find(otherObjectListener);

        slurper.parse(getResourcePath("borderTestCase1.xml"));

        // then
        XMLNode root = nodeFactory.createNode(0L, "ObjectTree", emptyMap());
        XMLNode object = nodeFactory.createNode(1L, "Object", emptyMap());
        XMLNode other = nodeFactory.createNode(2L, "OtherObject", emptyMap());
        XMLNode object2nd = nodeFactory.createNode(4L, "Object", emptyMap());
        XMLNode other2nd = nodeFactory.createNode(5L, "OtherObject", emptyMap());

        InOrder inOrder = inOrder(objectListener, otherObjectListener);
        inOrder.verify(objectListener).onNode(root, object);
        inOrder.verify(otherObjectListener, times(2)).onNode(object, other);
        inOrder.verify(objectListener).onNode(root, object2nd);
        inOrder.verify(otherObjectListener, times(2)).onNode(object2nd, other2nd);
        inOrder.verify(objectListener).onNode(root, object2nd);
        inOrder.verifyNoMoreInteractions();
    }

    private String getResourcePath(String resourceName) {
        return getClass().getResource(resourceName).getPath();
    }

    private void parse(String resourcePath, SlurpListener listener, String... nodePath) throws IOException, XMLStreamException {
        SlurpNode slurpNode = slurper.getNodes();

        for(String nodeName : nodePath)
            slurpNode = slurpNode.node(nodeName);

        slurpNode.find(listener);

        slurper.parse(getResourcePath(resourcePath));
    }
}
