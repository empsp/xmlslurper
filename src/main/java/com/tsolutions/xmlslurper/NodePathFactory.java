package com.tsolutions.xmlslurper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mturski on 11/13/2016.
 */
final class NodePathFactory {
    private static final String SIBLINGSCHECK_MARKER = "*";
    private static final String DEPTHCHECK_MARKER = "**";

    NodePath createEmpty() {
        return new RootNodePath();
    }

    NodePath copyNodePathAndAddNode(NodePath nodePath, String nodeName) {
        List<String> namePath = new ArrayList<String>(nodePath.getPath());
        namePath.add(nodeName);

        return getNodePath(namePath);
    }

    AttributeNodePath copyNodePathAndAddAttr(NodePath nodePath, String attrName) {
        List<String> namePath = new ArrayList<String>(nodePath.getPath());

        return new AttributeNodePathWrapper(getNodePath(namePath), attrName);
    }

    AttributeNodePath copyNodePathAndAddAttrValue(AttributeNodePath attrNodePath, String attrValue) {
        List<String> namePath = new ArrayList<String>(attrNodePath.getPath());

        return new AttributeValueNodePathWrapper(getNodePath(namePath), attrNodePath.getAttribute(), attrValue);
    }

    AttributeNodePath copyNodePathAndAddAttrExcludedValue(AttributeNodePath attrNodePath, String attrValue) {
        List<String> namePath = new ArrayList<String>(attrNodePath.getPath());

        return new AttributeExcludedValueNodePathWrapper(getNodePath(namePath), attrNodePath.getAttribute(), attrValue);
    }

    private NodePath getNodePath(List<String> namePath) {
        if (namePath.contains(SIBLINGSCHECK_MARKER))
            return new SiblingsNodePath(namePath);
        else
            return new SimpleNodePath(namePath);
    }

    private class RootNodePath extends NodePath {
        @Override
        public boolean checkAlignment(XMLNode node, int depthLevel) {
            return true;
        }

        @Override
        public List<String> getPath() {
            return new ArrayList<String>();
        }
    }

    private class SimpleNodePath extends NodePath {
        private final List<String> namePath;

        private boolean areMisaligned;
        private int misalignmentDepthLevel;

        private SimpleNodePath(List<String> namePath) {
            this.namePath = namePath;
        }

        @Override
        public boolean checkAlignment(XMLNode node, int depthLevel) {
            if (depthLevel > namePath.size()) // check if actual hierarchy depth is greater than expected
                return false;

            if (!areMisaligned) {
                if (namePath.get(depthLevel - 1).equals(node.getName())) {
                    // if partially aligned and names match, check if depths are the same, they are fully aligned if so
                    if (depthLevel == namePath.size())
                        return true;
                } else {
                    areMisaligned = true;
                    misalignmentDepthLevel = depthLevel;
                }
            } else if (depthLevel <= misalignmentDepthLevel && namePath.get(depthLevel - 1).equals(node.getName())) {
                // if were misaligned and names match but depth is less than misalignment depth, they again partially align
                areMisaligned = false;
            }

            return false;
        }

        @Override
        public List<String> getPath() {
            return namePath;
        }
    }

    private class SiblingsNodePath extends NodePath {
        private final List<String> namePath;

        private boolean areMisaligned;
        private int misalignmentDepthLevel;

        public SiblingsNodePath(List<String> namePath) {
            this.namePath = namePath;
        }

        @Override
        public boolean checkAlignment(XMLNode node, int depthLevel) {
            if (depthLevel > namePath.size()) // check if actual hierarchy depth is greater than expected
                return false;

            if (!areMisaligned) {
                String expectedNodeName = namePath.get(depthLevel - 1);
                if (expectedNodeName.equals(node.getName()) || expectedNodeName.equals(SIBLINGSCHECK_MARKER)) {
                    // if partially aligned and names match or siblings marker detected, check if depths are the same, they are fully aligned if so
                    if (depthLevel == namePath.size())
                        return true;
                } else {
                    areMisaligned = true;
                    misalignmentDepthLevel = depthLevel;
                }
            } else if (depthLevel <= misalignmentDepthLevel && namePath.get(depthLevel - 1).equals(node.getName())) {
                // if were misaligned and names match but depth is less than misalignment depth, they again partially align
                areMisaligned = false;
            }

            return false;
        }

        @Override
        public List<String> getPath() {
            return namePath;
        }
    }

    private class AttributeNodePathWrapper extends AttributeNodePath {
        private final NodePath nodePath;
        private final String attrName;

        public AttributeNodePathWrapper(NodePath nodePath, String attrName) {
            this.nodePath = nodePath;
            this.attrName = attrName;
        }

        @Override
        boolean checkAlignment(XMLNode node, int depthLevel) {
            return nodePath.checkAlignment(node, depthLevel) && node.hasAttribute(attrName);
        }

        @Override
        List<String> getPath() {
            return nodePath.getPath();
        }

        @Override
        String getAttribute() {
            return attrName;
        }
    }

    private class AttributeValueNodePathWrapper extends AttributeNodePath {
        private final NodePath nodePath;
        private final String attrName;
        private final String attrValue;

        public AttributeValueNodePathWrapper(NodePath nodePath, String attrName, String attrValue) {
            this.nodePath = nodePath;
            this.attrName = attrName;
            this.attrValue = attrValue;
        }

        @Override
        boolean checkAlignment(XMLNode node, int depthLevel) {
            return nodePath.checkAlignment(node, depthLevel) && attrValue.equals(node.getAttribute(attrName));
        }

        @Override
        List<String> getPath() {
            return nodePath.getPath();
        }

        @Override
        String getAttribute() {
            return attrName;
        }
    }

    private class AttributeExcludedValueNodePathWrapper extends AttributeNodePath {
        private final NodePath nodePath;
        private final String attrName;
        private final String attrValue;

        public AttributeExcludedValueNodePathWrapper(NodePath nodePath, String attrName, String attrValue) {
            this.nodePath = nodePath;
            this.attrName = attrName;
            this.attrValue = attrValue;
        }

        @Override
        boolean checkAlignment(XMLNode node, int depthLevel) {
            return nodePath.checkAlignment(node, depthLevel) && !attrValue.equals(node.getAttribute(attrName));
        }

        @Override
        List<String> getPath() {
            return nodePath.getPath();
        }

        @Override
        String getAttribute() {
            return attrName;
        }
    }
}
