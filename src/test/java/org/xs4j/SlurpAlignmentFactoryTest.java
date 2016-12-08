package org.xs4j;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by mturski on 11/17/2016.
 */
@SuppressWarnings("Duplicates")
public class SlurpAlignmentFactoryTest {
    private static long idFeed;

    private static final SlurpAlignmentFactory alignmentFactory = XMLSlurperFactory.getSlurpAlignmentFactory();
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private SlurpAlignment slurpAlignment;

    @Test
    public void givenRootCheckAlignmentReturnsTrueForRootNode() {
        getAlignment("Root");

        assertAlign("Root");
        assertNotAlign("Root", "Root");
        assertNotAlign("OtherRoot");
        assertAlign("Root");
        assertNotAlign("Root", "Child");
    }

    @Test
    public void givenChildOfRootCheckAlignmentReturnsTrueForAllPathsStartingWithRootHaving2ndLevelNodeMatchingName() {
        getAlignment("Root", "Child");

        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertNotAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Child");
        assertNotAlign("Root");
        assertNotAlign("Root", "Sibling");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child");
    }

    @Test
    public void givenChildrenOfRootCheckAlignmentReturnsTrueForAllPathsStartingWithRootHavingAny2ndLevel() {
        getAlignment("Root", "*");

        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("Root", "Sibling");
        assertNotAlign("Root", "Sibling", "Child");
        assertNotAlign("Root", "Sibling", "Kin");
        assertAlign("Root", "Child");
        assertNotAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Child");
    }

    @Test
    public void given2ndParticularChildOfRootCheckAlignmentReturnsTrueForEverySecondPathStartingWithRootHaving2ndLevelNodeMatchingName() {
        getAlignment("Root", "Kin");
        addNthNodeSelectionToAlignment(2);

        assertNotAlign("Root");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root");
        assertNotAlign("Root", "Kin");
        assertAlign("Root", "Kin");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root", "Kin", "Kin");
        assertAlign("Root", "Kin");
    }

    @Test
    public void given2ndChildOfRootCheckAlignmentReturnsTrueForEverySecondPathStartingWithRootHavingAnyAt2ndLevel() {
        getAlignment("Root", "*");
        addNthNodeSelectionToAlignment(2);

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Kin");
        assertNotAlign("Root", "Descendant");
        assertNotAlign("Root", "Offspring");
        assertNotAlign("Root");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root", "Kin", "Child");
        assertAlign("Root", "Child");
    }

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
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
        assertAlign("Child", "Kin", "Child", "Descendant");
        assertNotAlign("Child", "Kin", "Descendant");
        assertNotAlign("Child", "Child");
        assertAlign("Child", "Child", "Kin");
        assertAlign("Child", "Child", "Child");
        assertAlign("Child", "Child", "Child", "Child");
    }

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
    @Test
    public void givenChildrenOfDescendantsOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**", "**", "**", "*");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child", "Kin", "Offspring");
    }

    @Ignore
    @Test
    public void givenAll2ndNodesCheckAlignmentReturnsTrueForEverySecondNodeAtAnyDepth() {
        getAlignment("**");
        addNthNodeSelectionToAlignment(2);

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
        XMLNode lastNode = nodeFactory.createNode(idFeed++, nodes[nodes.length - 1], Collections.<String, String> emptyMap());

        assertThat(slurpAlignment.checkAlignment(nodes.length, lastNode), is(true));
    }

    private void assertNotAlign(String... nodes) {
        XMLNode lastNode = nodeFactory.createNode(idFeed++, nodes[nodes.length - 1], Collections.<String, String> emptyMap());

        assertThat(slurpAlignment.checkAlignment(nodes.length, lastNode), is(false));
    }

    private void getAlignment(String... nodes) {
        slurpAlignment = alignmentFactory.createAlignmentAndAddNodes(nodes);
    }

    private void addNthNodeSelectionToAlignment(long nodeIndex) {
        slurpAlignment = alignmentFactory.copyAlignmentAndSelectNthNode(slurpAlignment, nodeIndex);
    }
}
