package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.SlurpAttribute;
import com.tsolutions.xmlslurper.path.SlurpNode;
import com.tsolutions.xmlslurper.XMLSlurperFactory.SlurpAlignmentListenerTuple;

import java.util.List;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/11/2016.
 */
public class SlurpNodeImpl implements SlurpNode {
    private final List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples;
    private final SlurpAlignmentFactory slurpAlignmentFactory;
    private final SlurpAlignment slurpAlignment;

    SlurpNodeImpl(List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples, SlurpAlignmentFactory slurpAlignmentFactory, SlurpAlignment slurpAlignment) {
        this.slurpAlignmentListenerTuples = slurpAlignmentListenerTuples;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
        this.slurpAlignment = slurpAlignment;
    }

    @Override
    public SlurpNode node(@NotNull String name) {
        requireNonNull(name);

        return new SlurpNodeImpl(slurpAlignmentListenerTuples, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddNode(slurpAlignment, name));
    }

    @Override
    public SlurpAttribute attr(@NotNull String name) {
        requireNonNull(name);

        return new SlurpAttributeImpl(slurpAlignmentListenerTuples, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttribute(slurpAlignment, name));
    }

    @Override
    public void findAll(@NotNull SlurpListener slurpListener) {
        requireNonNull(slurpListener);

        slurpAlignmentListenerTuples.add(new SlurpAlignmentListenerTuple(slurpAlignment, slurpListener));
    }
}
