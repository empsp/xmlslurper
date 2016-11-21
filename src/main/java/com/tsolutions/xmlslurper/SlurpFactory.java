package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.XMLSlurperFactory.SlurpAlignmentListenerTuple;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.Slurp;
import com.tsolutions.xmlslurper.path.SlurpAttribute;
import com.tsolutions.xmlslurper.path.SlurpNode;

import java.util.List;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/21/2016.
 */
final class SlurpFactory {
    enum SlurpAlignmentType {
        IS, ISNOT;
    }

    private SlurpAlignmentFactory slurpAlignmentFactory;
    private List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples;

    SlurpFactory(SlurpAlignmentFactory slurpAlignmentFactory, List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples) {
        this.slurpAlignmentFactory = slurpAlignmentFactory;
        this.slurpAlignmentListenerTuples = slurpAlignmentListenerTuples;
    }

    SlurpNode createSlurpNode() {
        return new SlurpNodeImpl(slurpAlignmentFactory.createEmpty());
    }

    SlurpNode createSlurpNode(SlurpAlignment slurpAlignment, String localName) {
        return new SlurpNodeImpl(slurpAlignmentFactory.copyAlignmentAndAddNode(slurpAlignment, localName));
    }

    SlurpAttribute createSlurpAttribute(SlurpAlignment slurpAlignment, String attrName) {
        return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttribute(slurpAlignment, attrName));
    }

    SlurpAttribute createSlurpAttribute(SlurpAlignment slurpAlignment, String attrValue, SlurpAlignmentType slurpAlignmentType) {
        switch(slurpAlignmentType) {
            case IS:
                return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeValue(slurpAlignment, attrValue));
            case ISNOT:
                return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValue(slurpAlignment, attrValue));
        }

        throw new IllegalArgumentException();
    }

    private class SlurpNodeImpl implements SlurpNode {
        private final SlurpAlignment slurpAlignment;

        private SlurpNodeImpl(SlurpAlignment slurpAlignment) {
            this.slurpAlignment = slurpAlignment;
        }

        @Override
        public SlurpNode node(@NotNull String name) {
            requireNonNull(name);

            return createSlurpNode(slurpAlignment, name);
        }

        @Override
        public SlurpAttribute attr(@NotNull String name) {
            requireNonNull(name);

            return createSlurpAttribute(slurpAlignment, name);
        }

        @Override
        public void findAll(@NotNull SlurpListener slurpListener) {
            requireNonNull(slurpListener);

            slurpAlignmentListenerTuples.add(new SlurpAlignmentListenerTuple(slurpAlignment, slurpListener));
        }
    }

    private class SlurpAttributeImpl implements SlurpAttribute {
        private final SlurpAlignment slurpAlignment;

        private SlurpAttributeImpl(SlurpAlignment slurpAlignment) {
            this.slurpAlignment = slurpAlignment;
        }

        @Override
        public Slurp is(@NotNull String value) {
            requireNonNull(value);

            return createSlurpAttribute(slurpAlignment, value, SlurpAlignmentType.IS);
        }

        @Override
        public Slurp isNot(@NotNull String value) {
            requireNonNull(value);

            return createSlurpAttribute(slurpAlignment, value, SlurpAlignmentType.ISNOT);
        }

        @Override
        public void findAll(@NotNull SlurpListener slurpListener) {
            requireNonNull(slurpListener);

            slurpAlignmentListenerTuples.add(new SlurpAlignmentListenerTuple(slurpAlignment, slurpListener));
        }
    }
}
