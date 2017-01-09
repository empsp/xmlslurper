package org.xs4j.xmlslurper;

import org.xs4j.xmlslurper.NodeNotifier.FindAllData;
import org.xs4j.xmlslurper.NodeNotifier.FindOneData;
import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 11/21/2016.
 */
final class SlurpFactory {
    private final NodeNotifier nodeNotifier;
    private final SlurpAlignmentFactory slurpAlignmentFactory;

    SlurpFactory(NodeNotifier nodeNotifier, SlurpAlignmentFactory slurpAlignmentFactory) {
        this.nodeNotifier = nodeNotifier;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
    }

    SlurpNode createSlurpNode(String[] nodes) {
        if (nodes != null) {
            requireNonNull(nodes);

            return new SlurpNodeImpl(slurpAlignmentFactory.createAlignmentAndAddNodes(nodes));
        } else
            return new SlurpNodeImpl(slurpAlignmentFactory.createEmpty());
    }

    private class SlurpNodeImpl implements SlurpNode {
        private final SlurpAlignment slurpAlignment;

        private SlurpNodeImpl(SlurpAlignment slurpAlignment) {
            this.slurpAlignment = slurpAlignment;
        }

        @Override
        public SlurpNode node(@NotNull String qName) {
            requireNonNull(qName);

            return new SlurpNodeImpl(slurpAlignmentFactory.copyAlignmentAndAddNode(slurpAlignment, qName));
        }

        @Override
        public SlurpNode get(long nodeIndex) {
            return new SlurpNodeImpl(slurpAlignmentFactory.copyAlignmentAndSelectNthNode(slurpAlignment, nodeIndex));
        }

        @Override
        public SlurpAttribute attr(@NotNull String qName) {
            requireNonNull(qName);

            return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttribute(slurpAlignment, qName));
        }

        @Override
        public void find(@Nullable NodeListener nodeListener) {
            nodeNotifier.addFindData(new FindOneData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            nodeNotifier.addFindData(new FindOneData(slurpAlignment, startNodeListener, endNodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            nodeNotifier.addFindData(new FindAllData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            nodeNotifier.addFindData(new FindAllData(slurpAlignment, startNodeListener, endNodeListener));
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

            return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeValue(slurpAlignment, value));
        }

        @Override
        public Slurp startsWith(@NotNull String value) {
            requireNonNull(value);

            return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeStartsWithValue(slurpAlignment, value));
        }

        @Override
        public Slurp regex(@NotNull String regex) {
            requireNonNull(regex);

            return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeRegexValue(slurpAlignment, regex));
        }

        @Override
        public Slurp isNot(@NotNull String value) {
            requireNonNull(value);

            return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValue(slurpAlignment, value));
        }

        @Override
        public Slurp isNot(@NotNull String... values) {
            requireNonNull(values);

            return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValues(slurpAlignment, values));
        }

        @Override
        public void find(@Nullable NodeListener nodeListener) {
            nodeNotifier.addFindData(new FindOneData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            nodeNotifier.addFindData(new FindOneData(slurpAlignment, startNodeListener, endNodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            nodeNotifier.addFindData(new FindAllData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            nodeNotifier.addFindData(new FindAllData(slurpAlignment, startNodeListener, endNodeListener));
        }
    }
}
