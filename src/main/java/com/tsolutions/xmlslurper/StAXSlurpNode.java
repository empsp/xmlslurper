package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.SlurpAttribute;
import com.tsolutions.xmlslurper.path.SlurpNode;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/11/2016.
 */
public class StAXSlurpNode implements SlurpNode {
    private final StAXSlurper slurper;
    private SlurpAlignmentFactory slurpAlignmentFactory;
    private SlurpAlignment slurpAlignment;

    StAXSlurpNode(StAXSlurper slurper, SlurpAlignmentFactory slurpAlignmentFactory, SlurpAlignment slurpAlignment) {
        this.slurper = slurper;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
        this.slurpAlignment = slurpAlignment;
    }

    @Override
    public SlurpNode node(@NotNull String name) {
        requireNonNull(name);

        return new StAXSlurpNode(slurper, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddNode(slurpAlignment, name));
    }

    @Override
    public SlurpAttribute attr(@NotNull String name) {
        requireNonNull(name);

        return new StAXSlurpAttribute(slurper, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttribute(slurpAlignment, name));
    }

    @Override
    public void findAll(@NotNull SlurpListener slurpListener) {
        requireNonNull(slurpListener);

        slurper.registerSlurpListener(slurpAlignment, slurpListener);
    }
}
