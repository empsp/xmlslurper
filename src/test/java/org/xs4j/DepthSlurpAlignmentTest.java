package org.xs4j;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by mturski on 11/19/2016.
 */
@SuppressWarnings("Duplicates")
public class DepthSlurpAlignmentTest {
    private static long idFeed;

    private static final SlurpAlignmentFactory alignmentFactory = XMLSlurperFactory.getSlurpAlignmentFactory();
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private SlurpAlignment slurpAlignment;

    @Test
    public void givenRootDescendantsCheckAlignmentReturnTrueForAllNodesUnderRoot() {
        getAlignment("Root", "**");

        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("Root", "Child", "Child");
        assertAlign("Root", "Sibling");
        assertAlign("Root", "Sibling", "Kin");
        assertAlign("Root", "Child");
        assertNotAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Child");
    }

    @Test
    public void givenDescendantsHavingChildCheckAlignmentReturnsTrueForFirstChildInDescendantTree() {
        getAlignment("**", "Child");

        assertAlign("Child");
        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Sibling");
        assertAlign("Root", "Sibling", "Child");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Kin");
        assertNotAlign("Root", "Child", "Kin", "Child");
    }

    @Test
    public void givenDescendantsOfChildOfAnyRootCheckAlignmentReturnsTrueForAllNodesUnderChildUnderAnyRoot() {
        getAlignment("*", "Child", "**");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Child", "Descendant");
        assertNotAlign("Root", "Sibling");
        assertNotAlign("Root", "Sibling", "Kin");
        assertNotAlign("Root", "Child");
        assertNotAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Child");
        assertAlign("OtherRoot", "Child", "Descendant");
        assertAlign("OtherRoot", "Child", "Kin");
        assertAlign("OtherRoot", "Child", "Kin", "Offspring");
    }

    @Test
    public void givenChildrenOfDescendantsHavingChildCheckAlignmentReturnsTrueForImmediateChildrenOfFirstChildInDescendantTree() {
        getAlignment("**", "Child", "*");

        assertNotAlign("Child");
        assertAlign("Child", "Kin");
        assertNotAlign("Child", "Kin", "Descendant");
        assertAlign("Child", "Offspring");
        assertNotAlign("Root");
        assertNotAlign("Root", "Sibling");
        assertNotAlign("Root", "Sibling", "Child");
        assertAlign("Root", "Sibling", "Child", "Kin");
        assertAlign("Root", "Sibling", "Child", "Offspring");
        assertNotAlign("Root", "Sibling", "Child", "Offspring", "Descendant");
    }

    @Test
    public void givenDescendantsHavingNotImmediateChildCheckAlignmentReturnsTrueForSecondChildToAnyNodeInDescendantTree() {
        getAlignment("**", "*", "Child");

        assertNotAlign("Child");
        assertAlign("Child", "Child");
        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Kin");
        assertAlign("Root", "Kin", "Child");
        assertNotAlign("Root", "Kin", "Child", "Child");
        assertNotAlign("Root", "Kin", "Offspring");
        assertAlign("Root", "Kin", "Offspring", "Child");
        assertNotAlign("Root", "Kin", "Offspring", "Child", "Descendant");
    }

    @Test
    public void givenChildrenOfRootDescendantsCheckAlignmentReturnsTrueForAllNodesUnderRoot() {
        getAlignment("Child", "**", "*");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("Child");
        assertAlign("Child", "Descendant");
        assertAlign("Child", "Kin");
        assertNotAlign("Child", "Kin", "Offspring");
    }

    @Test
    public void givenChildrenOfDescendantsCheckAlignmentReturnsTrueForRootNodes() {
        getAlignment("**", "*");

        assertAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Kin");
        assertNotAlign("OtherRoot", "Kin", "Descendant");
        assertNotAlign("OtherRoot", "Child");
        assertAlign("Child");
    }

    @Test
    public void givenChildrenOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForRootAndChildren() {
        getAlignment("**", "**", "*");

        assertAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("OtherRoot");
        assertAlign("OtherRoot", "Kin");
        assertNotAlign("OtherRoot", "Kin", "Descendant");
        assertAlign("OtherRoot", "Offspring");
        assertAlign("Child");
    }

    @Test
    public void givenChildrenOfDescendantsOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForRootAndChildrenAndChildrenOfChildren() {
        getAlignment("**", "**", "**", "*");

        assertAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("Root", "Child", "Kin");
        assertNotAlign("Root", "Child", "Kin", "Offspring");
    }

    @Test
    public void givenAll2ndNodesCheckAlignmentReturnsTrueForEverySecondNodeAtAnyDepth() {
        getAlignment();
        addNthNodeToAlignment("**", 2);

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Kin");
        assertNotAlign("Root", "Kin", "Descendant");
        assertNotAlign("Root", "Kin", "Descendant", "Offspring");
        assertAlign("Root", "Kin", "Descendant", "Descendant");
        assertAlign("Root", "Kin", "Child");
        assertNotAlign("Root", "Kin", "Kin");
        assertNotAlign("Root", "Kin", "Child");
        assertNotAlign("Root", "Kin", "Kin");
        assertNotAlign("Root", "Child");
        assertAlign("OtherRoot");
    }

    @After
    public void teardown() {
        slurpAlignment = null;
    }

    private void assertAlign(String... nodes) {
        Deque<XMLNode> descendants = createDescendants(nodes);

        assertThat(slurpAlignment.checkAlignment(descendants, descendants.removeLast()), is(true));
    }

    private void assertNotAlign(String... nodes) {
        Deque<XMLNode> descendants = createDescendants(nodes);

        assertThat(slurpAlignment.checkAlignment(descendants, descendants.removeLast()), is(false));
    }

    private Deque<XMLNode> createDescendants(String... nodes) {
        Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

        for (String node : nodes)
            descendants.addLast(nodeFactory.createNode(idFeed++, node, Collections.<String, String> emptyMap()));

        return descendants;
    }

    private void getAlignment(String... nodes) {
        slurpAlignment = alignmentFactory.createAlignmentAndAddNodes(nodes);
    }

    private void addNthNodeToAlignment(String qName, long nodeIndex) {
        slurpAlignment = alignmentFactory.copyAlignmentAndAddNthNode(slurpAlignment, qName, nodeIndex);
    }
}
