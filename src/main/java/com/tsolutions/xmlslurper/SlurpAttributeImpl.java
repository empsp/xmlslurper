package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.SlurpAttribute;

import java.util.Map;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/14/2016.
 */
public class SlurpAttributeImpl implements SlurpAttribute {
    private final Map<SlurpAlignment, SlurpListener> slurpAlignmentListenerTuples;
    private final SlurpAlignmentFactory slurpAlignmentFactory;
    private final SlurpAttributeAlignment slurpAttributeAlignment;

    SlurpAttributeImpl(Map<SlurpAlignment, SlurpListener> slurpAlignmentListenerTuples, SlurpAlignmentFactory slurpAlignmentFactory, SlurpAttributeAlignment slurpAttributeAlignment) {
        this.slurpAlignmentListenerTuples = slurpAlignmentListenerTuples;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
        this.slurpAttributeAlignment = slurpAttributeAlignment;
    }

    @Override
    public SlurpAttribute is(@NotNull String value) {
        requireNonNull(value);

        return new SlurpAttributeImpl(slurpAlignmentListenerTuples, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttributeValue(slurpAttributeAlignment, value));
    }

    @Override
    public SlurpAttribute isNot(@NotNull String value) {
        requireNonNull(value);

        return new SlurpAttributeImpl(slurpAlignmentListenerTuples, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValue(slurpAttributeAlignment, value));
    }

    @Override
    public void findAll(@NotNull SlurpListener slurpListener) {
        requireNonNull(slurpListener);

        slurpAlignmentListenerTuples.put(slurpAttributeAlignment, slurpListener);
    }
}
