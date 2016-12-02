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
public class DepthSlurpAlignmentTest {
    private static long idFeed;

    private static final SlurpAlignmentFactory alignmentFactory = XMLSlurperFactory.getSlurpAlignmentFactory();
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private SlurpAlignment slurpAlignment;
    private Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();
    private XMLNode node;

    @Test
    public void givenRootDescendantsCheckAlignmentReturnTrueForAllNodesUnderRoot() {
        getAlignment("Root", "**");

        createAndAssertNotAlign("Root");
        addLevel();
        createAndAssertAlign("Child");
        addLevel();
        createAndAssertAlign("Child");
        removeLevel();
        createAndAssertAlign("Sibling");
        addLevel();
        createAndAssertAlign("Kin");
        removeLevel();
        createAndAssertAlign("Child");
        removeLevel();
        createAndAssertNotAlign("OtherRoot");
        addLevel();
        createAndAssertNotAlign("Child");
    }

    @Test
    public void givenDescendantsHavingChildCheckAlignmentReturnsTrueForFirstChildInDescendantTree() {
        getAlignment("**", "Child");

        createAndAssertAlign("Child");
        createAndAssertNotAlign("Root");
        addLevel();
        createAndAssertAlign("Child");
        createAndAssertNotAlign("Sibling");
        addLevel();
        createAndAssertAlign("Child");
        removeLevel();
        createAndAssertAlign("Child");
        addLevel();
        createAndAssertNotAlign("Kin");
        addLevel();
        createAndAssertNotAlign("Child");
    }

    @Test
    public void givenDescendantsOfChildOfAnyRootCheckAlignmentReturnsTrueForAllNodesUnderChildUnderAnyRoot() {
        getAlignment("*", "Child", "**");

        createAndAssertNotAlign("Root");
        addLevel();
        createAndAssertNotAlign("Child");
        addLevel();
        createAndAssertAlign("Descendant");
        removeLevel();
        createAndAssertNotAlign("Sibling");
        addLevel();
        createAndAssertNotAlign("Kin");
        removeLevel();
        createAndAssertNotAlign("Child");
        removeLevel();
        createAndAssertNotAlign("OtherRoot");
        addLevel();
        createAndAssertNotAlign("Child");
        addLevel();
        createAndAssertAlign("Descendant");
        createAndAssertAlign("Kin");
        addLevel();
        createAndAssertAlign("Offspring");
    }

    @Test
    public void givenChildrenOfDescendantsHavingChildCheckAlignmentReturnsTrueForImmediateChildrenOfFirstChildInDescendantTree() {
        getAlignment("**", "Child", "*");

        createAndAssertNotAlign("Child");
        addLevel();
        createAndAssertAlign("Kin");
        addLevel();
        createAndAssertNotAlign("Descendant");
        removeLevel();
        createAndAssertAlign("Offspring");
        removeLevel();
        createAndAssertNotAlign("Root");
        addLevel();
        createAndAssertNotAlign("Sibling");
        addLevel();
        createAndAssertNotAlign("Child");
        addLevel();
        createAndAssertAlign("Kin");
        createAndAssertAlign("Offspring");
        addLevel();
        createAndAssertNotAlign("Descendant");
    }

    @Test
    public void givenDescendantsHavingNotImmediateChildCheckAlignmentReturnsTrueForSecondChildToAnyNodeInDescendantTree() {
        getAlignment("**", "*", "Child");

        createAndAssertNotAlign("Child");
        addLevel();
        createAndAssertAlign("Child");
        removeLevel();
        createAndAssertNotAlign("Root");
        addLevel();
        createAndAssertAlign("Child");
        createAndAssertNotAlign("Kin");
        addLevel();
        createAndAssertAlign("Child");
        addLevel();
        createAndAssertNotAlign("Child");
        removeLevel();
        createAndAssertNotAlign("Offspring");
        addLevel();
        createAndAssertAlign("Child");
        addLevel();
        createAndAssertNotAlign("Descendant");
    }

    @Test
    public void givenChildrenOfRootDescendantsCheckAlignmentReturnsTrueForAllNodesUnderRoot() {
        getAlignment("Child", "**", "*");

        createAndAssertNotAlign("Root");
        addLevel();
        createAndAssertNotAlign("Child");
        removeLevel();
        createAndAssertNotAlign("Child");
        addLevel();
        createAndAssertAlign("Descendant");
        createAndAssertAlign("Kin");
        addLevel();
        createAndAssertNotAlign("Offspring");
    }

    @Test
    public void givenChildrenOfDescendantsCheckAlignmentReturnsTrueForRootNodes() {
        getAlignment("**", "*");

        createAndAssertAlign("Root");
        addLevel();
        createAndAssertNotAlign("Child");
        removeLevel();
        createAndAssertAlign("OtherRoot");
        addLevel();
        createAndAssertNotAlign("Kin");
        addLevel();
        createAndAssertNotAlign("Descendant");
        removeLevel();
        removeLevel();
        createAndAssertAlign("Child");
    }

    @Test
    public void givenChildrenOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForRootAndChildren() {
        getAlignment("**", "**", "*");

        createAndAssertAlign("Root");
        addLevel();
        createAndAssertAlign("Child");
        removeLevel();
        createAndAssertAlign("OtherRoot");
        addLevel();
        createAndAssertAlign("Kin");
        addLevel();
        createAndAssertNotAlign("Descendant");
        removeLevel();
        createAndAssertAlign("Offspring");
        removeLevel();
        createAndAssertAlign("Child");
    }

    @Test
    public void givenChildrenOfDescendantsOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForRootAndChildrenAndChildrenOfChildren() {
        getAlignment("**", "**", "**", "*");

        createAndAssertAlign("Root");
        addLevel();
        createAndAssertAlign("Child");
        addLevel();
        createAndAssertAlign("Kin");
        addLevel();
        createAndAssertNotAlign("Offspring");
    }

    @Test
    public void givenAll2ndNodesCheckAlignmentReturnsTrueForEverySecondNodeAtAnyDepth() {
        getAlignment();
        addNthNodeToAlignment("**", 2);

        createAndAssertNotAlign("Root");
        addLevel();
        createAndAssertNotAlign("Child");
        createAndAssertAlign("Kin");
        addLevel();
        createAndAssertNotAlign("Descendant");
        addLevel();
        createAndAssertNotAlign("Offspring");
        createAndAssertAlign("Descendant");
        removeLevel();
        createAndAssertAlign("Child");
        createAndAssertNotAlign("Kin");
        createAndAssertNotAlign("Child");
        createAndAssertNotAlign("Kin");
        removeLevel();
        createAndAssertNotAlign("Child");
        removeLevel();
        createAndAssertAlign("OtherRoot");
    }

    @After
    public void teardown() {
        slurpAlignment = null;
        descendants.clear();
    }

    private void addLevel() {
        descendants.addLast(node);
    }

    private void removeLevel() {
        descendants.removeLast();
    }

    private void createAndAssertAlign(String qName) {
        node = nodeFactory.createNode(idFeed++, qName, Collections.<String, String> emptyMap());

        assertThat(slurpAlignment.checkAlignment(descendants, node), is(true));
    }

    private void createAndAssertNotAlign(String qName) {
        node = nodeFactory.createNode(idFeed++, qName, Collections.<String, String> emptyMap());

        assertThat(slurpAlignment.checkAlignment(descendants, node), is(false));
    }

    private void getAlignment(String... nodes) {
        slurpAlignment = alignmentFactory.createAlignmentAndAddNodes(nodes);
    }

    private void addNthNodeToAlignment(String qName, long nodeIndex) {
        slurpAlignment = alignmentFactory.copyAlignmentAndAddNthNode(slurpAlignment, qName, nodeIndex);
    }
}
