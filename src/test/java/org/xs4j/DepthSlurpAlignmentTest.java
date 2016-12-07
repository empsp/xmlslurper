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
    public void givenDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**");

        assertAlign("Child");
        assertAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("Root", "Sibling");
        assertAlign("Root", "Sibling", "Child");
        assertAlign("Root", "Child");
        assertAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child", "Kin", "Child");
    }

    @Test
    public void givenDescendantsOfRootCheckAlignmentReturnsTrueForAllPathsStartingWithRootHavingAnyAt2ndLevelAndMore() {
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
    public void givenChildDescendantCheckAlignmentReturnsTrueForAllPathsEndingWithChild() {
        getAlignment("**", "Child");

        assertNotAlign("Child");
        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Sibling");
        assertAlign("Root", "Sibling", "Child");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child", "Kin", "Child");
    }

    @Test
    public void givenDescendantsOfChildBeingChildOfAnyRootCheckAlignmentReturnsTrueForAllPathStartingWithAnyHavingChildAt2ndLevelAnyAt3rdLevelAndMore() {
        getAlignment("*", "Child", "**");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Child", "Descendant");
        assertNotAlign("Root", "Sibling");
        assertNotAlign("Root", "Sibling", "Child");
        assertNotAlign("Root", "Sibling", "Child", "Kin");
        assertNotAlign("Root", "Sibling", "Kin");
        assertNotAlign("Root", "Child");
        assertNotAlign("Child");
        assertNotAlign("Child", "Child");
        assertAlign("Child", "Child", "Child");
        assertNotAlign("Child", "Kin");
        assertNotAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Child");
        assertAlign("OtherRoot", "Child", "Kin");
        assertAlign("OtherRoot", "Child", "Kin", "Offspring");
        assertAlign("OtherRoot", "Child", "Kin", "Child");
        assertAlign("OtherRoot", "Child", "Kin", "Child", "Offspring");
    }

    @Test
    public void givenChildrenOfChildBeingDescendantCheckAlignmentReturnsTrueForAllPathsHavingChildAtLevelBeforeLastEndingWithAny() {
        getAlignment("**", "Child", "*");

        assertNotAlign("Child");
        assertNotAlign("Child", "Kin");
        assertNotAlign("Child", "Kin", "Descendant");
        assertNotAlign("Child", "Offspring");
        assertNotAlign("Child", "Offspring", "Child");
        assertAlign("Child", "Offspring", "Child", "Kin");
        assertNotAlign("Root");
        assertNotAlign("Root", "Sibling");
        assertNotAlign("Root", "Sibling", "Child");
        assertAlign("Root", "Sibling", "Child", "Kin");
        assertAlign("Root", "Sibling", "Child", "Offspring");
        assertNotAlign("Root", "Sibling", "Child", "Offspring", "Descendant");
        assertNotAlign("Root", "Sibling", "Child", "Offspring", "Child");
        assertAlign("Root", "Sibling", "Child", "Offspring", "Child", "Descendant");
    }

    @Test
    public void givenDescendantsOfChildBeingDescendantCheckAlignmentReturnsTrueForAllPathsHavingChildAtMostLevelBeforeLast() {
        getAlignment("**", "Child", "**");

        assertNotAlign("Child");
        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child", "Kin", "Child");
        assertAlign("Root", "Child", "Kin", "Child", "Descendant");
        assertAlign("Root", "Child", "Kin", "Offspring");
        assertAlign("Root", "Child", "Offspring");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root", "Kin", "Child");
        assertAlign("Root", "Kin", "Child", "Offspring");
        assertNotAlign("Root", "Kin", "Offspring");
        assertNotAlign("Root", "Offspring");
        assertNotAlign("Child");
        assertNotAlign("Child", "Kin");
        assertNotAlign("Child", "Kin", "Descendant");
        assertNotAlign("Child", "Kin", "Descendant", "Offspring");
        assertNotAlign("Child", "Kin", "Child");
        assertNotAlign("Child", "Child");
        assertAlign("Child", "Child", "Kin");
        assertAlign("Child", "Child", "Child");
        assertAlign("Child", "Child", "Child", "Child");
    }

    @Test
    public void givenChildOfAnyDescendantCheckAlignmentReturnsTrueForAllPathsHavingAnyAtLevelBeforeLastEndingWithChild() {
        getAlignment("**", "*", "Child");

        assertNotAlign("Child");
        assertNotAlign("Child", "Child");
        assertAlign("Child", "Child", "Child");
        assertNotAlign("Child", "Child", "Kin");
        assertNotAlign("Child", "Kin");
        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root", "Kin");
        assertAlign("Root", "Kin", "Child");
        assertAlign("Root", "Kin", "Child", "Child");
        assertNotAlign("Root", "Kin", "Offspring");
        assertAlign("Root", "Kin", "Offspring", "Child");
        assertNotAlign("Root", "Kin", "Offspring", "Child", "Descendant");
        assertAlign("Root", "Kin", "Offspring", "Child", "Descendant", "Child");
    }

    @Test
    public void givenChildrenOfDescendantsOfRootCheckAlignmentReturnsTrueForPathsStartingWithRootHavingAnyAt2ndLevelAndMore() {
        getAlignment("Child", "**", "*");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Child");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Child");
        assertNotAlign("Child", "Descendant");
        assertNotAlign("Child", "Kin");
        assertAlign("Child", "Kin", "Offspring");
        assertAlign("Child", "Kin", "Offspring", "Child");
    }

    @Test
    public void givenChildrendOfDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**", "*");

        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertNotAlign("OtherRoot");
        assertAlign("OtherRoot", "Kin");
        assertAlign("OtherRoot", "Kin", "Descendant");
        assertAlign("OtherRoot", "Child");
        assertNotAlign("Child");
    }

    @Test
    public void givenChildrenOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**", "**", "*");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Kin");
        assertAlign("OtherRoot", "Kin", "Descendant");
        assertNotAlign("OtherRoot", "Offspring");
        assertNotAlign("Child");
    }

    @Test
    public void givenChildrenOfDescendantsOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**", "**", "**", "*");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child", "Kin", "Offspring");
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
