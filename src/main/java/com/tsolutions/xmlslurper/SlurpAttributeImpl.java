package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.XMLSlurperFactory.SlurpAlignmentListenerTuple;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.Slurp;
import com.tsolutions.xmlslurper.path.SlurpAttribute;

import java.util.List;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/14/2016.
 */
public class SlurpAttributeImpl implements SlurpAttribute {
    private final List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples;
    private final SlurpAlignmentFactory slurpAlignmentFactory;
    private final SlurpAlignment slurpAlignment;

    SlurpAttributeImpl(List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples, SlurpAlignmentFactory slurpAlignmentFactory, SlurpAlignment slurpAlignment) {
        this.slurpAlignmentListenerTuples = slurpAlignmentListenerTuples;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
        this.slurpAlignment = slurpAlignment;
    }

    @Override
    public Slurp is(@NotNull String value) {
        requireNonNull(value);

        return new SlurpAttributeImpl(slurpAlignmentListenerTuples, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttributeValue(slurpAlignment, value));
    }

    @Override
    public Slurp isNot(@NotNull String value) {
        requireNonNull(value);

        return new SlurpAttributeImpl(slurpAlignmentListenerTuples, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValue(slurpAlignment, value));
    }

    @Override
    public void findAll(@NotNull SlurpListener slurpListener) {
        requireNonNull(slurpListener);

        slurpAlignmentListenerTuples.add(new SlurpAlignmentListenerTuple(slurpAlignment, slurpListener));
    }
}
