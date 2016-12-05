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
    public void givenRootChildCheckAlignmentReturnsTrueForRootChild() {
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
    public void givenRootChildrenCheckAlignmentReturnsTrueForAllImmediateRootChildren() {
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
    public void given2ndRootChildCheckAlignmentReturnsTrueFor2ndRootChild() {
        getAlignment("Root");
        addNthNodeToAlignment("Child", 2);

        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Child");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertNotAlign("Root", "Child", "Child");
        assertAlign("Root", "Child");
    }

    @Test
    public void givenRoot2ndOfChildrenCheckAlignmentReturnsTrueForRoot2ndOfChildren() {
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

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
    @Test
    public void givenDescendantsOfChildBeingDescendantCheckAlignmentReturnsTrueForPathsContainingChildNode() {
        getAlignment("**", "Child", "**");

        assertNotAlign("Child");
        assertNotAlign("Root");
        assertNotAlign("Root", "Child");
        assertAlign("Root", "Child", "Kin");
        assertNotAlign("Root", "Kin");
        assertNotAlign("Root", "Kin", "Child");
        assertAlign("Root", "Kin", "Child", "Offspring");
        assertNotAlign("Root", "Kin", "Offspring");
        assertNotAlign("Root", "Offspring");
        assertNotAlign("Child");
        assertAlign("Child", "Kin");
        assertAlign("Child", "Kin", "Descendant");
        assertAlign("Child", "Kin", "Descendant", "Offspring");
        assertAlign("Child", "Kin", "Child");
        assertAlign("Child", "Child");
    }

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
    @Test
    public void givenChildrenOfDescendantsOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForRootAndChildrenAndChildrenOfChildren() {
        getAlignment("**", "**", "**", "*");

        assertAlign("Root");
        assertAlign("Root", "Child");
        assertAlign("Root", "Child", "Kin");
        assertNotAlign("Root", "Child", "Kin", "Offspring");
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
