package com.tsolutions.xmlslurper;

import java.util.ArrayList;
import java.util.List;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/12/2016.
 */
public class NodePathHandler {
    private final List<String> nodePath;

    private boolean areNodePathsAligned;

    NodePathHandler(List<String> nodePath) {
        this.nodePath = requireNonNull(nodePath);
    }

    boolean areNodePathsAligned(XMLNode child, int depthLevel) {
        // is this SlurpNode the initial one, if yes then all paths are aligned
        if (nodePath.isEmpty())
            return true;

        // is actual node deeper than set nodePath, if yes they are misaligned
        if (depthLevel > nodePath.size())
            return false;

        // is actual nodeName equal to set nodeName on that depth level, if yes path are aligned but not yet fully aligned
        if (nodePath.get(depthLevel - 1).equals(child.getName())) {
            areNodePathsAligned = true;

            // if depth matches nodePath size then paths are fully aligned
            if (nodePath.size() == depthLevel)
                return true;
        }

        return false;
    }

    List<String> createNodePathFromExisting(String additionalNodeName) {
        requireNonNull(additionalNodeName);

        ArrayList<String> newNodePath = new ArrayList<>(nodePath);
        newNodePath.add(additionalNodeName);
        return newNodePath;
    }
}
