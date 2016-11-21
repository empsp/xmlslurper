package com.tsolutions.xmlslurper;

import org.junit.After;
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
        slurpAlignment = getAlignment("Root", "**");

        createAndAssertNotAlign("Root");
        addAsLast();
        createAndAssertAlign("Child");
        addAsLast();
        createAndAssertAlign("Child");
        removeLast();
        createAndAssertAlign("Sibling");
        addAsLast();
        createAndAssertAlign("Kin");
        removeLast();
        createAndAssertAlign("Child");
        removeLast();
        createAndAssertNotAlign("OtherRoot");
        addAsLast();
        createAndAssertNotAlign("Child");
    }

    @Test
    public void givenDescendantsHavingChildCheckAlignmentReturnsTrueForFirstChildInDescendantTree() {
        slurpAlignment = getAlignment("**", "Child");

        createAndAssertNotAlign("Root");
        addAsLast();
        createAndAssertAlign("Child");
        createAndAssertNotAlign("Sibling");
        addAsLast();
        createAndAssertAlign("Child");
        removeLast();
        createAndAssertAlign("Child");
        addAsLast();
        createAndAssertNotAlign("Kin");
        addAsLast();
        createAndAssertNotAlign("Child");
    }

    @Test
    public void givenDescendantsOfChildOfAnyRootCheckAlignmentReturnsTrueForAllNodesUnderChildUnderAnyRoot() {
        slurpAlignment = getAlignment("*", "Child", "**");

        createAndAssertNotAlign("Root");
        addAsLast();
        createAndAssertNotAlign("Child");
        addAsLast();
        createAndAssertAlign("Descendant");
        removeLast();
        createAndAssertNotAlign("Sibling");
        addAsLast();
        createAndAssertNotAlign("Kin");
        removeLast();
        createAndAssertNotAlign("Child");
        removeLast();
        createAndAssertNotAlign("OtherRoot");
        addAsLast();
        createAndAssertNotAlign("Child");
        addAsLast();
        createAndAssertAlign("Descendant");
        createAndAssertAlign("Kin");
        addAsLast();
        createAndAssertAlign("Offspring");
    }

    @Test
    public void givenChildrenOfDescendantsHavingChildCheckAlignmentReturnsTrueForImmediateChildrenOfFirstChildInDescendantTree() {
        slurpAlignment = getAlignment("**", "Child", "*");

        createAndAssertNotAlign("Child");
        addAsLast();
        createAndAssertAlign("Kin");
        addAsLast();
        createAndAssertNotAlign("Descendant");
        removeLast();
        createAndAssertAlign("Offspring");
        removeLast();
        createAndAssertNotAlign("Root");
        addAsLast();
        createAndAssertNotAlign("Sibling");
        addAsLast();
        createAndAssertNotAlign("Child");
        addAsLast();
        createAndAssertAlign("Kin");
        createAndAssertAlign("Offspring");
        addAsLast();
        createAndAssertNotAlign("Descendant");
    }

    @Test
    public void givenDescendantsHavingNotImmediateChildCheckAlignmentReturnsTrueForSecondChildToAnyNodeInDescendantTree() {
        slurpAlignment = getAlignment("**", "*", "Child");

        createAndAssertNotAlign("Child");
        addAsLast();
        createAndAssertAlign("Child");
        removeLast();
        createAndAssertNotAlign("Root");
        addAsLast();
        createAndAssertAlign("Child");
        createAndAssertNotAlign("Kin");
        addAsLast();
        createAndAssertAlign("Child");
        addAsLast();
        createAndAssertNotAlign("Child");
        removeLast();
        createAndAssertNotAlign("Offspring");
        addAsLast();
        createAndAssertAlign("Child");
        addAsLast();
        createAndAssertNotAlign("Descendant");
    }

    @After
    public void teardown() {
        slurpAlignment = null;
        descendants.clear();
    }

    private void addAsLast() {
        descendants.addLast(node);
    }

    private void removeLast() {
        descendants.removeLast();
    }

    private void createAndAssertAlign(String localName) {
        node = nodeFactory.createNode(idFeed++, localName, Collections.emptyMap());

        assertThat(slurpAlignment.checkAlignment(descendants, node), is(true));
    }

    private void createAndAssertNotAlign(String localName) {
        node = nodeFactory.createNode(idFeed++, localName, Collections.emptyMap());

        assertThat(slurpAlignment.checkAlignment(descendants, node), is(false));
    }

    private SlurpAlignment getAlignment(String... nodeLocalNames) {
        SlurpAlignment alignment = alignmentFactory.createEmpty();

        for(String nodeLocalName : nodeLocalNames) {
            alignment = alignmentFactory.copyAlignmentAndAddNode(alignment, nodeLocalName);
        }

        return alignment;
    }
}
