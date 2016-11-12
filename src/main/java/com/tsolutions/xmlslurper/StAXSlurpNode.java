package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.event.SlurpListener;
import com.tsolutions.xmlslurper.path.Node;
import com.tsolutions.xmlslurper.path.SlurpAttribute;
import com.tsolutions.xmlslurper.path.SlurpExcludedNode;
import com.tsolutions.xmlslurper.path.SlurpNode;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/11/2016.
 */
public class StAXSlurpNode implements SlurpNode {
    private final StAXSlurper slurper;
    private final List<String> nodePath;

    private boolean areNodePathsAligned;

    StAXSlurpNode(StAXSlurper slurper, List<String> nodePath) {
        this.slurper = slurper;
        this.nodePath = requireNonNull(nodePath);
    }

    @Override
    public SlurpNode node(@NotNull String name) {
        requireNonNull(name);

        ArrayList<String> nodePath = new ArrayList<>(this.nodePath);
        nodePath.add(name);
        return new StAXSlurpNode(slurper, nodePath);
    }

    @Override
    public SlurpExcludedNode notNode(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SlurpAttribute attr(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void find(SlurpListener slurpListener) throws XMLStreamException {
        slurper.searchFor(this, slurpListener);
    }

    boolean areNodePathsAligned(Node parent, Node child, int depthLevel) {
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
}
