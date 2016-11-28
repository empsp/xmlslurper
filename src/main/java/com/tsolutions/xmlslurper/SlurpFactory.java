package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.tsolutions.xmlslurper.NodeNotifier.NodeNotifierData;
import com.tsolutions.xmlslurper.listener.NodeListener;
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
        IS, ISNOT, REGEX
    }

    private final List<NodeNotifierData> findData;
    private final List<NodeNotifierData> findAllData;
    private final SlurpAlignmentFactory slurpAlignmentFactory;

    SlurpFactory(List<NodeNotifierData> findData, List<NodeNotifierData> findAllData, SlurpAlignmentFactory slurpAlignmentFactory) {
        this.findData = findData;
        this.findAllData = findAllData;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
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

    SlurpAttribute createSlurpAttribute(SlurpAlignmentType slurpAlignmentType, SlurpAlignment slurpAlignment, String attrValue) {
        switch(slurpAlignmentType) {
            case IS:
                return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeValue(slurpAlignment, attrValue));
            case ISNOT:
                return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValue(slurpAlignment, attrValue));
            case REGEX:
                return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeRegexValue(slurpAlignment, attrValue));
        }

        throw new IllegalArgumentException();
    }

    SlurpAttribute createSlurpAttribute(SlurpAlignment slurpAlignment, String[] attrValues) {
        return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValues(slurpAlignment, attrValues));
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
        public void find(@Nullable NodeListener nodeListener) {
            findData.add(new NodeNotifierData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findData.add(new NodeNotifierData(slurpAlignment, startNodeListener, endNodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            findAllData.add(new NodeNotifierData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findAllData.add(new NodeNotifierData(slurpAlignment, startNodeListener, endNodeListener));
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

            return createSlurpAttribute(SlurpAlignmentType.IS, slurpAlignment, value);
        }

        @Override
        public Slurp regex(@NotNull String regex) {
            requireNonNull(regex);

            return createSlurpAttribute(SlurpAlignmentType.REGEX, slurpAlignment, regex);
        }

        @Override
        public Slurp isNot(@NotNull String value) {
            requireNonNull(value);

            return createSlurpAttribute(SlurpAlignmentType.ISNOT, slurpAlignment, value);
        }

        @Override
        public Slurp isNot(@NotNull String... values) {
            requireNonNull(values);

            return createSlurpAttribute(slurpAlignment, values);
        }

        @Override
        public void find(@Nullable NodeListener nodeListener) {
            findData.add(new NodeNotifierData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findData.add(new NodeNotifierData(slurpAlignment, startNodeListener, endNodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            findAllData.add(new NodeNotifierData(slurpAlignment, nodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findAllData.add(new NodeNotifierData(slurpAlignment, startNodeListener, endNodeListener));
        }
    }
}
