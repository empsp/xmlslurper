package com.tsolutions.xmlslurper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mturski on 11/13/2016.
 */
final class SlurpAlignmentFactory {
    private static final String SIBLINGSCHECK_MARKER = "*";
    private static final String DEPTHCHECK_MARKER = "**";

    SlurpAlignment createEmpty() {
        return new DefaultSlurpAlignment();
    }

    SlurpAlignment copyAlignmentAndAddNode(SlurpAlignment slurpAlignment, String nodeName) {
        List<String> namePath = new ArrayList<String>(slurpAlignment.getPath());
        namePath.add(nodeName);

        return getSlurpAlignment(namePath);
    }

    SlurpAlignment copyAlignmentAndAddAttribute(SlurpAlignment slurpAlignment, String attrName) {
        List<String> namePath = new ArrayList<String>(slurpAlignment.getPath());

        return new SlurpAttributeAlignmentWrapper(getSlurpAlignment(namePath), attrName);
    }

    SlurpAlignment copyAlignmentAndAddAttributeValue(SlurpAlignment slurpAlignment, String attrValue) {
        List<String> namePath = new ArrayList<String>(slurpAlignment.getPath());

        return new ValueSlurpAttributeAlignmentWrapper(getSlurpAlignment(namePath), ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrName, attrValue);
    }

    SlurpAlignment copyAlignmentAndAddAttributeExcludedValue(SlurpAlignment slurpAlignment, String attrValue) {
        List<String> namePath = new ArrayList<String>(slurpAlignment.getPath());

        return new ExcludedValueSlurpAttributeAlignmentWrapper(getSlurpAlignment(namePath), ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrName, attrValue);
    }

    private SlurpAlignment getSlurpAlignment(List<String> namePath) {
        if (namePath.contains(SIBLINGSCHECK_MARKER))
            return new SiblingsSlurpAlignment(namePath);
        else
            return new SimpleSlurpAlignment(namePath);
    }

    private class DefaultSlurpAlignment extends SlurpAlignment {
        @Override
        public boolean checkAlignment(XMLNode node, int depthLevel) {
            return true;
        }

        @Override
        public List<String> getPath() {
            return new ArrayList<String>();
        }
    }

    private class SimpleSlurpAlignment extends SlurpAlignment {
        private final List<String> namePath;

        private int misalignmentDepthLevel = Integer.MAX_VALUE;

        private SimpleSlurpAlignment(List<String> namePath) {
            this.namePath = namePath;
        }

        @Override
        public boolean checkAlignment(XMLNode node, int depthLevel) {
            if (depthLevel > misalignmentDepthLevel || depthLevel > namePath.size())
                return false;

            if (namePath.get(depthLevel - 1).equals(node.getName())) {
                misalignmentDepthLevel = Integer.MAX_VALUE;

                if (depthLevel == namePath.size()) // if partially aligned and names match, check if depths are the same, they are fully aligned if so
                    return true;
            } else
                misalignmentDepthLevel = depthLevel;

            return false;
        }

        @Override
        public List<String> getPath() {
            return namePath;
        }
    }

    private class SiblingsSlurpAlignment extends SlurpAlignment {
        private final List<String> namePath;

        private int misalignmentDepthLevel;

        public SiblingsSlurpAlignment(List<String> namePath) {
            this.namePath = namePath;
        }

        @Override
        public boolean checkAlignment(XMLNode node, int depthLevel) {
            if (depthLevel > misalignmentDepthLevel || depthLevel > namePath.size())
                return false;

            String expectedNodeName = namePath.get(depthLevel - 1);
            if (expectedNodeName.equals(node.getName()) || expectedNodeName.equals(SIBLINGSCHECK_MARKER)) {
                misalignmentDepthLevel = Integer.MAX_VALUE;

                if (depthLevel == namePath.size()) // if partially aligned and names match, check if depths are the same, they are fully aligned if so
                    return true;
            } else
                misalignmentDepthLevel = depthLevel;

            return false;
        }

        @Override
        public List<String> getPath() {
            return namePath;
        }
    }

    private class SlurpAttributeAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final String attrName;

        public SlurpAttributeAlignmentWrapper(SlurpAlignment slurpAlignment, String attrName) {
            this.slurpAlignment = slurpAlignment;
            this.attrName = attrName;
        }

        @Override
        boolean checkAlignment(XMLNode node, int depthLevel) {
            return slurpAlignment.checkAlignment(node, depthLevel) && node.hasAttribute(attrName);
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }

    private class ValueSlurpAttributeAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final String attrName;
        private final String attrValue;

        public ValueSlurpAttributeAlignmentWrapper(SlurpAlignment slurpAlignment, String attrName, String attrValue) {
            this.slurpAlignment = slurpAlignment;
            this.attrName = attrName;
            this.attrValue = attrValue;
        }

        @Override
        boolean checkAlignment(XMLNode node, int depthLevel) {
            return slurpAlignment.checkAlignment(node, depthLevel) && attrValue.equals(node.getAttribute(attrName));
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }

    private class ExcludedValueSlurpAttributeAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final String attrName;
        private final String attrValue;

        public ExcludedValueSlurpAttributeAlignmentWrapper(SlurpAlignment slurpAlignment, String attrName, String attrValue) {
            this.slurpAlignment = slurpAlignment;
            this.attrName = attrName;
            this.attrValue = attrValue;
        }

        @Override
        boolean checkAlignment(XMLNode node, int depthLevel) {
            return slurpAlignment.checkAlignment(node, depthLevel) && !attrValue.equals(node.getAttribute(attrName));
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }
}
