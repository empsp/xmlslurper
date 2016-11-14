package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.path.SlurpAttribute;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/14/2016.
 */
public class StAXSlurpAttribute implements SlurpAttribute {
    private final StAXSlurper slurper;
    private final NodePathFactory nodePathFactory;
    private AttributeNodePath attrNodePath;

    StAXSlurpAttribute(StAXSlurper slurper, NodePathFactory nodePathFactory, AttributeNodePath attrNodePath) {
        this.slurper = slurper;
        this.nodePathFactory = nodePathFactory;
        this.attrNodePath = attrNodePath;
    }

    @Override
    public SlurpAttribute is(@NotNull String value) {
        requireNonNull(value);

        return new StAXSlurpAttribute(slurper, nodePathFactory, nodePathFactory.copyNodePathAndAddAttrValue(attrNodePath, value));
    }

    @Override
    public SlurpAttribute isNot(@NotNull String value) {
        requireNonNull(value);

        return new StAXSlurpAttribute(slurper, nodePathFactory, nodePathFactory.copyNodePathAndAddAttrExcludedValue(attrNodePath, value));
    }

    @Override
    public void findAll(@NotNull SlurpListener slurpListener) {
        requireNonNull(slurpListener);

        slurper.registerSlurpListener(attrNodePath, slurpListener);
    }
}
