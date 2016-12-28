package org.xs4j;

import org.xs4j.NodeNotifier.FindData;
import org.xs4j.listener.NodeListener;
import org.xs4j.path.Slurp;
import org.xs4j.path.SlurpAttribute;
import org.xs4j.path.SlurpNode;
import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 11/21/2016.
 */
final class SlurpFactory {
    private final Deque<FindData> findData;
    private final Deque<FindData> findAllData;
    private final SlurpAlignmentFactory slurpAlignmentFactory;

    SlurpFactory(Deque<FindData> findData, Deque<FindData> findAllData, SlurpAlignmentFactory slurpAlignmentFactory) {
        this.findData = findData;
        this.findAllData = findAllData;
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
        private final Deque<FindData> slurpData;

        private SlurpNodeImpl(SlurpAlignment slurpAlignment) {
            this.slurpAlignment = slurpAlignment;
            this.slurpData = new ArrayDeque<FindData>();
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
            FindData data = new FindData(slurpAlignment, nodeListener, nodeListener);

            findData.addLast(data);
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            FindData data = new FindData(slurpAlignment, startNodeListener, endNodeListener);

            findData.addLast(data);
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            FindData data = new FindData(slurpAlignment, nodeListener, nodeListener);

            slurpData.addLast(data);
            findAllData.addLast(data);
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            FindData data = new FindData(slurpAlignment, startNodeListener, endNodeListener);

            slurpData.addLast(data);
            findAllData.addLast(data);
        }

        @Override
        public void stopFindAll() {
            findAllData.removeAll(slurpData);
        }
    }

    private class SlurpAttributeImpl implements SlurpAttribute {
        private final SlurpAlignment slurpAlignment;
        private final Deque<FindData> slurpData;

        private SlurpAttributeImpl(SlurpAlignment slurpAlignment) {
            this.slurpAlignment = slurpAlignment;
            this.slurpData = new ArrayDeque<FindData>();
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
            FindData data = new FindData(slurpAlignment, nodeListener, nodeListener);

            findData.addLast(data);
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            FindData data = new FindData(slurpAlignment, startNodeListener, endNodeListener);

            findData.addLast(data);
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            FindData data = new FindData(slurpAlignment, nodeListener);

            slurpData.addLast(data);
            findAllData.addLast(data);
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            FindData data = new FindData(slurpAlignment, startNodeListener, endNodeListener);

            slurpData.addLast(data);
            findAllData.addLast(data);
        }

        @Override
        public void stopFindAll() {
            findAllData.removeAll(slurpData);
        }
    }
}
