package com.tsolutions.xmlslurper;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mturski on 11/17/2016.
 */
public class SlurpAlignmentFactoryTest {
    private static long idFeed;

    private static final SlurpAlignmentFactory alignmentFactory = XMLSlurperFactory.getSlurpAlignmentFactory();
    private static final NodeFactory nodeFactory = XMLSlurperFactory.getNodeFactory();

    @Test
    public void givenRootAlignmentCheckAlignmentReturnsTrueOnlyWhenNamesAreEqualOnRootDepthLevel() {
        // when
        SlurpAlignment alignment = alignmentFactory.copyAlignmentAndAddNode(alignmentFactory.createEmpty(), "Root");

        // then

        // paths align
        assertTrue(alignment.checkAlignment(createNode("Root"), 1));

        // element named 'Root' deeper than on root level
        assertFalse(alignment.checkAlignment(createNode("Root"), 2));

        // root other than 'Root'
        assertFalse(alignment.checkAlignment(createNode("Other"), 1));

        // root's child
        assertTrue(alignment.checkAlignment(createNode("Root"), 1));
        assertFalse(alignment.checkAlignment(createNode("Child"), 2));
    }

    @Test
    public void givenChildAlignmentCheckAlignmentReturnsTrueOnlyWhenPathsAreEqualOnRespectiveDepthLevel() {
        // when
        SlurpAlignment alignment = alignmentFactory.copyAlignmentAndAddNode(
                alignmentFactory.copyAlignmentAndAddNode(alignmentFactory.createEmpty(), "Root"), "Child");

        // then

        // paths align
        assertFalse(alignment.checkAlignment(createNode("Root"), 1));
        assertTrue(alignment.checkAlignment(createNode("Child"), 2));

        // root other than 'Root'
        assertFalse(alignment.checkAlignment(createNode("OtherRoot"), 1));
        assertFalse(alignment.checkAlignment(createNode("Child"), 2));

        // sibling and then the paths align
        assertFalse(alignment.checkAlignment(createNode("Root"), 1));
        assertFalse(alignment.checkAlignment(createNode("Sibling"), 2));
        assertTrue(alignment.checkAlignment(createNode("Child"), 2));

        // sibling's child named 'Child' and other child and then the paths align
        assertFalse(alignment.checkAlignment(createNode("Root"), 1));
        assertFalse(alignment.checkAlignment(createNode("Sibling"), 2));
        assertFalse(alignment.checkAlignment(createNode("Child"), 3));
        assertFalse(alignment.checkAlignment(createNode("SiblingsChild"), 3));
        assertTrue(alignment.checkAlignment(createNode("Child"), 2));
    }

    @Test
    public void givenSiblingAlignmentCheckAlignmentReturnsTrueOnlyWhenPathsAreEqualOnRespectiveDepthLevel() {
        // when
        SlurpAlignment alignment = alignmentFactory.copyAlignmentAndAddNode(
                alignmentFactory.copyAlignmentAndAddNode(alignmentFactory.createEmpty(), "Root"), "*");

        // then

        // paths align
        assertFalse(alignment.checkAlignment(createNode("Root"), 1));
        assertTrue(alignment.checkAlignment(createNode("Child"), 2));

        // root other than 'Root'
        assertFalse(alignment.checkAlignment(createNode("OtherRoot"), 1));
        assertFalse(alignment.checkAlignment(createNode("Child"), 2));

        // sibling and then the paths align
        assertFalse(alignment.checkAlignment(createNode("Root"), 1));
        assertTrue(alignment.checkAlignment(createNode("Sibling"), 2));
        assertTrue(alignment.checkAlignment(createNode("Child"), 2));

        // sibling's child named 'Child' and other child and then the paths align
        assertFalse(alignment.checkAlignment(createNode("Root"), 1));
        assertTrue(alignment.checkAlignment(createNode("Sibling"), 2));
        assertFalse(alignment.checkAlignment(createNode("Child"), 3));
        assertFalse(alignment.checkAlignment(createNode("SiblingsChild"), 3));
        assertTrue(alignment.checkAlignment(createNode("Child"), 2));
    }

    private XMLNode createNode(String name) {
        return nodeFactory.createNode(idFeed++, name, Collections.emptyMap());
    }
}
