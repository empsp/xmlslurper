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
public class SlurpAlignmentFactoryTest {
    private static long idFeed;

    private static final SlurpAlignmentFactory alignmentFactory = XMLSlurperFactory.getSlurpAlignmentFactory();
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    private SlurpAlignment slurpAlignment;

    @Test
    public void givenRootCheckAlignmentReturnsTrueForRootNode() {
        slurpAlignment = getAlignment("Root");

        assertAlign("Root", 1);
        assertNotAlign("Root", 2);
        assertNotAlign("OtherRoot", 1);
        assertAlign("Root", 1);
        assertNotAlign("Child", 2);
    }

    @Test
    public void givenRootChildCheckAlignmentReturnsTrueForRootChild() {
        slurpAlignment = getAlignment("Root", "Child");

        assertNotAlign("Root", 1);
        assertAlign("Child", 2);
        assertNotAlign("OtherRoot", 1);
        assertNotAlign("Child", 2);
        assertNotAlign("Root", 1);
        assertNotAlign("Sibling", 2);
        assertAlign("Child", 2);
        assertNotAlign("Kin", 3);
        assertAlign("Child", 2);
    }

    @Test
    public void givenRootChildrenCheckAlignmentReturnsTrueForAllImmediateRootChildren() {
        slurpAlignment = getAlignment("Root", "*");

        assertNotAlign("Root", 1);
        assertAlign("Child", 2);
        assertAlign("Sibling", 2);
        assertNotAlign("Child", 3);
        assertNotAlign("Kin", 3);
        assertAlign("Child", 2);
        assertNotAlign("OtherRoot", 1);
        assertNotAlign("Child", 2);
    }

    @Ignore
    @Test
    public void givenRootDescendantsCheckAlignmentReturnTrueForAllNodesUnderRoot() {
        slurpAlignment = getAlignment("Root", "**");

        assertNotAlign("Root", 1);
        assertAlign("Child", 2);
        assertAlign("Child", 3);
        assertAlign("Sibling", 2);
        assertAlign("Kin", 3);
        assertAlign("Child", 2);
        assertNotAlign("OtherRoot", 1);
        assertNotAlign("Child", 2);
    }

    @Ignore
    @Test
    public void givenDescendantsOfChildOfAnyRootCheckAlignmentReturnsTrueForAllNodesUnderChildUnderAnyRoot() {
        slurpAlignment = getAlignment("*", "Child", "**");

        assertNotAlign("Root", 1);
        assertNotAlign("Child", 2);
        assertAlign("Descendant", 3);
        assertNotAlign("Sibling", 2);
        assertNotAlign("Kin", 3);
        assertNotAlign("Child", 2);
        assertNotAlign("OtherRoot", 1);
        assertNotAlign("Child", 2);
        assertAlign("Descendant", 3);
        assertAlign("Kin", 3);
        assertAlign("Offspring", 4);
    }

    @Ignore
    @Test
    public void givenDescendantsHavingChildCheckAlignmentReturnsTrueForFirstChildInDescendantTree() {
        slurpAlignment = getAlignment("**", "Child");

        assertAlign("Child", 1);
        assertNotAlign("Root", 1);
        assertAlign("Child", 2);
        assertNotAlign("Sibling", 2);
        assertAlign("Child", 3);
        assertAlign("Child", 2);
        assertNotAlign("Kin", 3);
        assertNotAlign("Child", 4);
    }

    @Ignore
    @Test
    public void givenChildrenOfDescendantsHavingChildCheckAlignmentReturnsTrueForImmediateChildrenOfFirstChildInDescendantTree() {
        slurpAlignment = getAlignment("**", "Child", "*");

        assertNotAlign("Child", 1);
        assertAlign("Kin", 2);
        assertNotAlign("Descendant", 3);
        assertAlign("Offspring", 2);
        assertNotAlign("Root", 1);
        assertNotAlign("Sibling", 2);
        assertNotAlign("Child", 3);
        assertAlign("Kin", 4);
        assertAlign("Offspring", 4);
        assertNotAlign("Descendant", 5);
    }

    @Ignore
    @Test
    public void givenDescendantsHavingNotImmediateChildCheckAlignmentReturnsTrueForSecondChildToAnyNodeInDescendantTree() {
        slurpAlignment = getAlignment("**", "*", "Child");

        assertNotAlign("Child", 1);
        assertAlign("Child", 2);
        assertNotAlign("Root", 1);
        assertAlign("Child", 2);
        assertNotAlign("Kin", 2);
        assertAlign("Child", 3);
        assertNotAlign("Child", 4);
        assertNotAlign("Offspring", 3);
        assertAlign("Child", 4);
        assertNotAlign("Descendant", 5);
    }

    @Ignore
    @Test
    public void givenChildrenOfRootDescendantsCheckAlignmentReturnsTrueForAllNodesUnderRoot() {
        slurpAlignment = getAlignment("Child", "**", "*");

        assertNotAlign("Root", 1);
        assertNotAlign("Child", 2);
        assertNotAlign("Child", 1);
        assertAlign("Descendant", 2);
        assertAlign("Kin", 2);
        assertNotAlign("Offspring", 3);
    }

    @Ignore
    @Test
    public void givenChildrenOfDescendantsCheckAlignmentReturnsTrueForRootNodes() {
        slurpAlignment = getAlignment("**", "*");

        assertAlign("Root", 1);
        assertAlign("Child", 1);
        assertNotAlign("Child", 2);
        assertAlign("OtherRoot", 1);
        assertNotAlign("Kin", 2);
        assertNotAlign("Descendant", 3);
    }

    @Ignore
    @Test
    public void givenChildrenOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForRootAndChildren() {
        slurpAlignment = getAlignment("**", "**", "*");

        assertAlign("Root", 1);
        assertAlign("Child", 2);
        assertAlign("OtherRoot", 1);
        assertAlign("Kin", 2);
        assertNotAlign("Descendant", 3);
        assertAlign("Offspring", 2);
        assertAlign("Child", 1);
    }

    @Ignore
    @Test
    public void givenChildrenOfDescendantsOfDescendantsOfDescendantsCheckAlignmentReturnsTrueForRootAndChildrenAndChildrenOfChildren() {
        slurpAlignment = getAlignment("**", "**", "**", "*");

        assertAlign("Root", 1);
        assertAlign("Child", 2);
        assertAlign("Kin", 3);
        assertNotAlign("Offspring", 4);
    }

    @After
    public void teardown() {
        slurpAlignment = null;
    }

    private void assertAlign(String localName, int depth) {
        XMLNode node = nodeFactory.createNode(idFeed++, localName, Collections.emptyMap());

        assertThat(slurpAlignment.checkAlignment(depth, node), is(true));
    }

    private void assertNotAlign(String localName, int depth) {
        XMLNode node = nodeFactory.createNode(idFeed++, localName, Collections.emptyMap());

        assertThat(slurpAlignment.checkAlignment(depth, node), is(false));
    }

    private SlurpAlignment getAlignment(String... nodeLocalNames) {
        SlurpAlignment alignment = alignmentFactory.createEmpty();

        for(String nodeLocalName : nodeLocalNames) {
            alignment = alignmentFactory.copyAlignmentAndAddNode(alignment, nodeLocalName);
        }

        return alignment;
    }
}
