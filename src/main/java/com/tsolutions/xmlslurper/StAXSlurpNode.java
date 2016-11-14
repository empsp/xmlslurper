package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.path.SlurpAttribute;
import com.tsolutions.xmlslurper.path.SlurpNode;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/11/2016.
 */
public class StAXSlurpNode implements SlurpNode {
    private final StAXSlurper slurper;
    private NodePathFactory nodePathFactory;
    private NodePath nodePath;

    StAXSlurpNode(StAXSlurper slurper, NodePathFactory nodePathFactory, NodePath nodePath) {
        this.slurper = slurper;
        this.nodePathFactory = nodePathFactory;
        this.nodePath = nodePath;
    }

    @Override
    public SlurpNode node(@NotNull String name) {
        requireNonNull(name);

        return new StAXSlurpNode(slurper, nodePathFactory, nodePathFactory.copyNodePathAndAddNode(nodePath, name));
    }

    @Override
    public SlurpAttribute attr(@NotNull String name) {
        requireNonNull(name);

        return new StAXSlurpAttribute(slurper, nodePathFactory, nodePathFactory.copyNodePathAndAddAttr(nodePath, name));
    }

    @Override
    public void findAll(@NotNull SlurpListener slurpListener) {
        requireNonNull(slurpListener);

        slurper.registerSlurpListener(nodePath, slurpListener);
    }
}
