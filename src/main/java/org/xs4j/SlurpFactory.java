package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xs4j.NodeNotifier.FindData;
import org.xs4j.listener.NodeListener;
import org.xs4j.path.Slurp;
import org.xs4j.path.SlurpAttribute;
import org.xs4j.path.SlurpNode;

import java.util.List;

import static org.xs4j.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/21/2016.
 */
final class SlurpFactory {
    private final List<FindData> findData;
    private final List<FindData> findAllData;
    private final SlurpAlignmentFactory slurpAlignmentFactory;

    SlurpFactory(List<FindData> findData, List<FindData> findAllData, SlurpAlignmentFactory slurpAlignmentFactory) {
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

        private SlurpNodeImpl(SlurpAlignment slurpAlignment) {
            this.slurpAlignment = slurpAlignment;
        }

        @Override
        public SlurpNode node(@NotNull String qName) {
            requireNonNull(qName);

            return new SlurpNodeImpl(slurpAlignmentFactory.copyAlignmentAndAddNode(slurpAlignment, qName));
        }

        @Override
        public SlurpNode node(@NotNull String qName, long nodeIndex) {
            return new SlurpNodeImpl(slurpAlignmentFactory.copyAlignmentAndAddNthNode(slurpAlignment, qName, nodeIndex));
        }

        @Override
        public SlurpAttribute attr(@NotNull String qName) {
            requireNonNull(qName);

            return new SlurpAttributeImpl(slurpAlignmentFactory.copyAlignmentAndAddAttribute(slurpAlignment, qName));
        }

        @Override
        public void find(@Nullable NodeListener nodeListener) {
            findData.add(new FindData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findData.add(new FindData(slurpAlignment, startNodeListener, endNodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            findAllData.add(new FindData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findAllData.add(new FindData(slurpAlignment, startNodeListener, endNodeListener));
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
            findData.add(new FindData(slurpAlignment, nodeListener, nodeListener));
        }

        @Override
        public void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findData.add(new FindData(slurpAlignment, startNodeListener, endNodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener nodeListener) {
            findAllData.add(new FindData(slurpAlignment, nodeListener));
        }

        @Override
        public void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener) {
            findAllData.add(new FindData(slurpAlignment, startNodeListener, endNodeListener));
        }
    }
}
