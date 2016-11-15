package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.listener.SlurpListener;
import com.tsolutions.xmlslurper.path.SlurpAttribute;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/14/2016.
 */
public class StAXSlurpAttribute implements SlurpAttribute {
    private final StAXSlurper slurper;
    private final SlurpAlignmentFactory slurpAlignmentFactory;
    private SlurpAttributeAlignment attrNodePath;

    StAXSlurpAttribute(StAXSlurper slurper, SlurpAlignmentFactory slurpAlignmentFactory, SlurpAttributeAlignment attrNodePath) {
        this.slurper = slurper;
        this.slurpAlignmentFactory = slurpAlignmentFactory;
        this.attrNodePath = attrNodePath;
    }

    @Override
    public SlurpAttribute is(@NotNull String value) {
        requireNonNull(value);

        return new StAXSlurpAttribute(slurper, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttributeValue(attrNodePath, value));
    }

    @Override
    public SlurpAttribute isNot(@NotNull String value) {
        requireNonNull(value);

        return new StAXSlurpAttribute(slurper, slurpAlignmentFactory, slurpAlignmentFactory.copyAlignmentAndAddAttributeExcludedValue(attrNodePath, value));
    }

    @Override
    public void findAll(@NotNull SlurpListener slurpListener) {
        requireNonNull(slurpListener);

        slurper.registerSlurpListener(attrNodePath, slurpListener);
    }
}
