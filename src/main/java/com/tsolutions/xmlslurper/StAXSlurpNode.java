package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.path.SlurpAttribute;
import com.tsolutions.xmlslurper.path.SlurpNode;

import java.util.List;

/**
 * Created by mturski on 11/11/2016.
 */
public class StAXSlurpNode implements SlurpNode {
    private final StAXSlurper slurper;
    private final NodePathHandler nodePathHandler;

    StAXSlurpNode(StAXSlurper slurper, List<String> nodePath) {
        this.slurper = slurper;
        this.nodePathHandler = new NodePathHandler(nodePath);
    }

    @Override
    public SlurpNode node(@NotNull String name) {
        return new StAXSlurpNode(slurper, nodePathHandler.createNodePathFromExisting(name));
    }

    @Override
    public SlurpAttribute attr(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void find(SlurpListener slurpListener) {
        slurper.registerSlurpListener(nodePathHandler, slurpListener);
    }
}
