package com.tsolutions.xmlslurper;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mturski on 11/13/2016.
 */
final class SlurpAlignmentFactory {
    private static final String SIBLING_MARKER = "*";
    private static final String DEPTH_MARKER = "**";

    SlurpAlignmentFactory() {
    }

    SlurpAlignment createEmpty() {
        return new DefaultSlurpAlignment();
    }

    SlurpAlignment copyAlignmentAndAddNode(SlurpAlignment slurpAlignment, String localName) {
        List<String> localNamePath = new ArrayList<String>(slurpAlignment.getPath());
        localNamePath.add(localName);

        return getSlurpAlignment(localNamePath);
    }

    SlurpAlignment copyAlignmentAndAddAttribute(SlurpAlignment slurpAlignment, String attrName) {
        List<String> localNamePath = new ArrayList<String>(slurpAlignment.getPath());

        return new SlurpAttributeAlignmentWrapper(getSlurpAlignment(localNamePath), attrName);
    }

    SlurpAlignment copyAlignmentAndAddAttributeValue(SlurpAlignment slurpAlignment, String attrValue) {
        List<String> localNamePath = new ArrayList<String>(slurpAlignment.getPath());

        return new ValueSlurpAttributeAlignmentWrapper(getSlurpAlignment(localNamePath), ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrName, attrValue);
    }

    SlurpAlignment copyAlignmentAndAddAttributeExcludedValue(SlurpAlignment slurpAlignment, String attrValue) {
        List<String> localNamePath = new ArrayList<String>(slurpAlignment.getPath());

        return new ExcludedValueSlurpAttributeAlignmentWrapper(getSlurpAlignment(localNamePath), ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrName, attrValue);
    }

    private SlurpAlignment getSlurpAlignment(List<String> localNamePath) {
        if (localNamePath.contains(DEPTH_MARKER))
            return new DepthSlurpAlignment(localNamePath);
        else
            return new SimpleSlurpAlignment(localNamePath);
    }

    private class DefaultSlurpAlignment extends SlurpAlignment {
        @Override
        public boolean checkAlignment(int depth, XMLNode lastNode) {
            return true;
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            return true;
        }

        @Override
        public List<String> getPath() {
            return new ArrayList<String>();
        }
    }

    private class SimpleSlurpAlignment extends SlurpAlignment {
        private final List<String> localNamePath;

        private int misalignmentDepth = Integer.MAX_VALUE;

        private SimpleSlurpAlignment(List<String> localNamePath) {
            this.localNamePath = localNamePath;
        }

        @Override
        public boolean checkAlignment(int depth, XMLNode lastNode) {
            if (depth > misalignmentDepth || depth > localNamePath.size())
                return false;

            String name = localNamePath.get(depth - 1);
            if (name.equals(lastNode.getLocalName()) || name.equals(SIBLING_MARKER)) {
                misalignmentDepth = Integer.MAX_VALUE;

                if (depth == localNamePath.size()) // paths are aligned only when all path names match and sizes are equal
                    return true;
            } else
                misalignmentDepth = depth;

            return false;
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            return checkAlignment(descendants.size() + 1, lastNode); // +1 for lastNode which is a detached part of descendants
        }

        @Override
        public List<String> getPath() {
            return localNamePath;
        }
    }

    private class DepthSlurpAlignment extends SlurpAlignment {
        private final List<String> localNamePath;

        public DepthSlurpAlignment(List<String> localNamePath) {
            this.localNamePath = localNamePath;
        }

        @Override
        public boolean checkAlignment(int depth, XMLNode lastNode) {
            throw new UnsupportedOperationException(); // TODO unsupported yet
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            Iterator<String> nameIt = localNamePath.iterator();
            String name = nameIt.next();
            boolean isPrevNameDepthMarker = false;
            boolean isSiblingAfterDepthMarker = false;

            for (XMLNode descendant : descendants) {
                if (name.equals(DEPTH_MARKER)) {
                    if (!nameIt.hasNext())
                        return true;

                    isPrevNameDepthMarker = true;
                    name = nameIt.next();
                }

                if (name.equals(SIBLING_MARKER)) {
                    if (!nameIt.hasNext())
                        return false;

                    if (isPrevNameDepthMarker)
                        isSiblingAfterDepthMarker = true;

                    isPrevNameDepthMarker = false;
                    name = nameIt.next();
                } else if (name.equals(descendant.getLocalName())) {
                    if (!nameIt.hasNext())
                        return false;

                    isSiblingAfterDepthMarker = false;
                    isPrevNameDepthMarker = false;
                    name = nameIt.next();
                } else if (!isPrevNameDepthMarker && !isSiblingAfterDepthMarker)
                    return false;
            }

            return !nameIt.hasNext() && (name.equals(lastNode.getLocalName()) || name.equals(SIBLING_MARKER) || name.equals(DEPTH_MARKER));
        }

        @Override
        public List<String> getPath() {
            return localNamePath;
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
        boolean checkAlignment(int depth, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(depth, lastNode) && lastNode.hasAttribute(attrName);
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(descendants, lastNode) && lastNode.hasAttribute(attrName);
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
        boolean checkAlignment(int depth, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(depth, lastNode) && attrValue.equals(lastNode.getAttribute(attrName));
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(descendants, lastNode) && attrValue.equals(lastNode.getAttribute(attrName));
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
        boolean checkAlignment(int depth, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(depth, lastNode) && !attrValue.equals(lastNode.getAttribute(attrName));
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(descendants, lastNode) && !attrValue.equals(lastNode.getAttribute(attrName));
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }
}
