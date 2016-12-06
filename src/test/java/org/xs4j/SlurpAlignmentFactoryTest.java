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
        getAlignment("Root");
        addNthNodeToAlignment("Kin", 2); //changed

        assertNotAlign("Root");
        assertNotAlign("Root", "Kin"); //changed
        assertAlign("Root", "Kin"); //changed
        assertNotAlign("Root", "Kin"); //changed
        assertNotAlign("Root", "Kin"); //changed
        assertNotAlign("Root");
        assertNotAlign("Root", "Kin"); //changed
        assertNotAlign("Root", "Kin", "Kin"); //changed
        assertAlign("Root", "Kin"); //changed
    }

    @Test
    public void given2ndChildOfRootCheckAlignmentReturnsTrueForEverySecondPathStartingWithRootHavingAnyAt2ndLevel() {
        getAlignment("Root");
        addNthNodeToAlignment("*", 2);

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

        assertAlign("Child");
        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Sibling");
        assertAlign("Root", "Sibling", "Child");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child", "Kin", "Child"); //changed
    }

    @Ignore
    @Test
    public void givenDescendantsOfChildBeingChildOfAnyRootCheckAlignmentReturnsTrueForAllPathStartingWithAnyHavingChildAt2ndLevelAnyAt3rdLevelAndMore() {
        getAlignment("*", "Child", "**");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Child", "Descendant");
        assertNotAlign("Root", "Sibling");
        assertNotAlign("Root", "Sibling", "Kin");
        assertNotAlign("Root", "Child");
        assertNotAlign("Child"); //added
        assertNotAlign("Child", "Child"); //added
        assertAlign("Child", "Child", "Child"); //added
        assertNotAlign("Child", "Kin"); //added
        assertNotAlign("OtherRoot");
        assertNotAlign("OtherRoot", "Child");
        assertAlign("OtherRoot", "Child", "Descendant");
        assertAlign("OtherRoot", "Child", "Kin");
        assertAlign("OtherRoot", "Child", "Kin", "Offspring");
        assertNotAlign("OtherRoot", "Child", "Kin", "Child"); //added
        assertAlign("OtherRoot", "Child", "Kin", "Child", "Offspring"); //added
    }

    @Ignore
    @Test
    public void givenChildrenOfChildBeingDescendantCheckAlignmentReturnsTrueForAllPathsHavingChildAtLevelBeforeLastEndingWithAny() {
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
        assertNotAlign("Root", "Sibling", "Child", "Offspring", "Child"); //added
        assertAlign("Root", "Sibling", "Child", "Offspring", "Child", "Descendant"); //added
    }

    @Ignore
    @Test
    public void givenDescendantsOfChildBeingDescendantCheckAlignmentReturnsTrueForAllPathsHavingChildAtMostLevelBeforeLast() {
        getAlignment("**", "Child", "**");

        assertNotAlign("Child");
        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Child", "Kin");
        assertNotAlign("Root", "Child", "Kin", "Child"); //added
        assertAlign("Root", "Child", "Kin", "Child", "Descendant"); //added
        assertAlign("Root", "Child", "Kin", "Offspring"); //added
        assertAlign("Root", "Child", "Offspring"); //added
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root", "Kin", "Child");
        assertAlign("Root", "Kin", "Child", "Offspring");
        assertNotAlign("Root", "Kin", "Offspring");
        assertNotAlign("Root", "Offspring");
        assertNotAlign("Child");
        assertAlign("Child", "Kin");
        assertAlign("Child", "Kin", "Descendant");
        assertAlign("Child", "Kin", "Descendant", "Offspring");
        assertNotAlign("Child", "Kin", "Child"); //changed
        assertNotAlign("Child", "Child"); //changed
        assertAlign("Child", "Child", "Kin"); //added
    }

    @Ignore
    @Test
    public void givenChildOfAnyDescendantCheckAlignmentReturnsTrueForAllPathsHavingAnyAtLevelBeforeLastEndingWithChild() {
        getAlignment("**", "*", "Child");

        assertNotAlign("Child");
        assertAlign("Child", "Child");
        assertAlign("Child", "Child", "Child"); //added
        assertNotAlign("Child", "Child", "Kin"); //added
        assertNotAlign("Child", "Kin"); //added
        assertNotAlign("Root");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Kin");
        assertAlign("Root", "Kin", "Child");
        assertAlign("Root", "Kin", "Child", "Child"); //changed
        assertNotAlign("Root", "Kin", "Offspring");
        assertAlign("Root", "Kin", "Offspring", "Child");
        assertNotAlign("Root", "Kin", "Offspring", "Child", "Descendant");
        assertAlign("Root", "Kin", "Offspring", "Child", "Descendant", "Child"); //added
    }

    @Ignore
    @Test
    public void givenChildrenOfDescendantsOfRootCheckAlignmentReturnsTrueForPathsStartingWithRootHavingAnyAt2ndLevelAndMore() {
        getAlignment("Child", "**", "*");

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Child"); //added
        assertNotAlign("Root", "Kin"); //added
        assertNotAlign("Child");
        assertAlign("Child", "Descendant");
        assertAlign("Child", "Kin");
        assertAlign("Child", "Kin", "Offspring"); //changed
        assertAlign("Child", "Kin", "Offspring", "Child"); //added
    }

    @Ignore
    @Test
    public void givenChildrendOfDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**", "*");

        assertAlign("Root");
        assertAlign("Root", "Child"); //changed
        assertAlign("OtherRoot");
        assertAlign("OtherRoot", "Kin"); //changed
        assertAlign("OtherRoot", "Kin", "Descendant"); //changed
        assertAlign("OtherRoot", "Child"); //changed
        assertAlign("Child");
    }

    @Ignore
    @Test
    public void givenChildrenOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**", "**", "*");

        assertAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("OtherRoot");
        assertAlign("OtherRoot", "Kin");
        assertAlign("OtherRoot", "Kin", "Descendant"); //changed
        assertAlign("OtherRoot", "Offspring");
        assertAlign("Child");
    }

    @Ignore
    @Test
    public void givenChildrenOfDescendantsOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForAllPaths() {
        getAlignment("**", "**", "**", "*");

        assertAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("Root", "Child", "Kin");
        assertAlign("Root", "Child", "Kin", "Offspring"); //changed
    }

    @Ignore
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

    private void addNthNodeToAlignment(String qName, long nodeIndex) {
        slurpAlignment = alignmentFactory.copyAlignmentAndAddNthNode(slurpAlignment, qName, nodeIndex);
    }
}
