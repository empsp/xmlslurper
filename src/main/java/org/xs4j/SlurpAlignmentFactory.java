package org.xs4j;

import java.util.*;
import java.util.regex.Pattern;

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

    SlurpAlignment createAlignmentAndAddNodes(String[] qNames) {
        List<String> qNamePath = new ArrayList<String>(Arrays.asList(qNames));

        return getSlurpAlignment(qNamePath);
    }

    SlurpAlignment copyAlignmentAndAddNode(SlurpAlignment slurpAlignment, String qName) {
        List<String> qNamePath = new ArrayList<String>(slurpAlignment.getPath());
        qNamePath.add(qName);

        return getSlurpAlignment(qNamePath);
    }

    SlurpAlignment copyAlignmentAndAddNthNode(SlurpAlignment slurpAlignment, String qName, long nodeIndex) {
        List<String> qNamePath = new ArrayList<String>(slurpAlignment.getPath());
        qNamePath.add(qName);

        return new SlurpAlignmentNthElementWrapper(getSlurpAlignment(qNamePath), nodeIndex);
    }

    SlurpAlignment copyAlignmentAndAddAttribute(SlurpAlignment slurpAlignment, String attrQName) {
        List<String> qNamePath = new ArrayList<String>(slurpAlignment.getPath());

        return new SlurpAttributeAlignmentWrapper(
                getSlurpAlignment(qNamePath),
                attrQName);
    }

    SlurpAlignment copyAlignmentAndAddAttributeValue(SlurpAlignment slurpAlignment, String attrValue) {
        List<String> qNamePath = new ArrayList<String>(slurpAlignment.getPath());

        return new ValueSlurpAttributeAlignmentWrapper(
                getSlurpAlignment(qNamePath),
                ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrQName,
                attrValue);
    }

    SlurpAlignment copyAlignmentAndAddAttributeExcludedValue(SlurpAlignment slurpAlignment, String attrValue) {
        List<String> qNamePath = new ArrayList<String>(slurpAlignment.getPath());

        return new ExcludedValuesSlurpAttributeAlignmentWrapper(
                getSlurpAlignment(qNamePath),
                ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrQName,
                new String[] {attrValue});
    }

    SlurpAlignment copyAlignmentAndAddAttributeExcludedValues(SlurpAlignment slurpAlignment, String[] attrValues) {
        List<String> qNamePath = new ArrayList<String>(slurpAlignment.getPath());
        String[] copiedAttrValues = Arrays.copyOf(attrValues, attrValues.length);

        return new ExcludedValuesSlurpAttributeAlignmentWrapper(
                getSlurpAlignment(qNamePath),
                ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrQName,
                copiedAttrValues);
    }

    SlurpAlignment copyAlignmentAndAddAttributeRegexValue(SlurpAlignment slurpAlignment, String regex) {
        List<String> qNamePath = new ArrayList<String>(slurpAlignment.getPath());
        Pattern valuePattern = Pattern.compile(regex);

        return new RegexValueSlurpAttributeAlignmentWrapper(
                getSlurpAlignment(qNamePath),
                ((SlurpAttributeAlignmentWrapper)slurpAlignment).attrQName,
                valuePattern);
    }

    private SlurpAlignment getSlurpAlignment(List<String> qNamePath) {
        if (qNamePath.contains(DEPTH_MARKER))
            return new DepthSlurpAlignment(qNamePath);
        else if (qNamePath.isEmpty())
            return new DefaultSlurpAlignment();
        else
            return new SimpleSlurpAlignment(qNamePath);
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
        private final List<String> namePath;

        private int misalignmentDepth = Integer.MAX_VALUE;

        private SimpleSlurpAlignment(List<String> namePath) {
            this.namePath = namePath;
        }

        @Override
        public boolean checkAlignment(int depth, XMLNode lastNode) {
            if (depth > misalignmentDepth || depth > namePath.size())
                return false;

            String name = namePath.get(depth - 1);
            if (name.equals(lastNode.getQName()) || name.equals(SIBLING_MARKER)) {
                misalignmentDepth = Integer.MAX_VALUE;

                if (depth == namePath.size()) // paths are aligned only when all path names match and sizes are equal
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
            return namePath;
        }
    }

    private class DepthSlurpAlignment extends SlurpAlignment {
        private final List<String> namePath;

        public DepthSlurpAlignment(List<String> namePath) {
            this.namePath = namePath;
        }

        @Override
        public boolean checkAlignment(int depth, XMLNode lastNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            Iterator<String> nameIt = namePath.iterator();
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
                } else if (name.equals(descendant.getQName())) {
                    if (!nameIt.hasNext())
                        return false;

                    isSiblingAfterDepthMarker = false;
                    isPrevNameDepthMarker = false;
                    name = nameIt.next();
                } else if (!isPrevNameDepthMarker && !isSiblingAfterDepthMarker)
                    return false;
            }

            while (name.equals(DEPTH_MARKER) && nameIt.hasNext())
                name = nameIt.next();

            return !nameIt.hasNext() && (name.equals(lastNode.getQName()) || name.equals(SIBLING_MARKER) || name.equals(DEPTH_MARKER));
        }

        @Override
        public List<String> getPath() {
            return namePath;
        }
    }

    private class SlurpAlignmentNthElementWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final long nodeIndex;

        public SlurpAlignmentNthElementWrapper(SlurpAlignment slurpAlignment, long nodeIndex) {
            this.slurpAlignment = slurpAlignment;
            this.nodeIndex = nodeIndex;
        }

        @Override
        boolean checkAlignment(int depth, XMLNode lastNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }

    private class SlurpAttributeAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final String attrQName;

        public SlurpAttributeAlignmentWrapper(SlurpAlignment slurpAlignment, String attrQName) {
            this.slurpAlignment = slurpAlignment;
            this.attrQName = attrQName;
        }

        @Override
        boolean checkAlignment(int depth, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(depth, lastNode) && lastNode.hasAttribute(attrQName);
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(descendants, lastNode) && lastNode.hasAttribute(attrQName);
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }

    private class ValueSlurpAttributeAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final String attrQName;
        private final String attrValue;

        public ValueSlurpAttributeAlignmentWrapper(SlurpAlignment slurpAlignment, String attrQName, String attrValue) {
            this.slurpAlignment = slurpAlignment;
            this.attrQName = attrQName;
            this.attrValue = attrValue;
        }

        @Override
        boolean checkAlignment(int depth, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(depth, lastNode) && attrValue.equals(lastNode.getAttribute(attrQName));
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            return slurpAlignment.checkAlignment(descendants, lastNode) && attrValue.equals(lastNode.getAttribute(attrQName));
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }

    private class ExcludedValuesSlurpAttributeAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final String attrQName;
        private final String[] attrValues;

        public ExcludedValuesSlurpAttributeAlignmentWrapper(SlurpAlignment slurpAlignment, String attrQName, String[] attrValues) {
            this.slurpAlignment = slurpAlignment;
            this.attrQName = attrQName;
            this.attrValues = attrValues;
        }

        @Override
        boolean checkAlignment(int depth, XMLNode lastNode) {
            if (slurpAlignment.checkAlignment(depth, lastNode)) {
                String actualAttrValue = lastNode.getAttribute(attrQName);

                return actualAttrValue != null && isAttributeValueOutsideExcludedRange(actualAttrValue);
            }

            return false;
        }

        private boolean isAttributeValueOutsideExcludedRange(String actualAttrValue) {
            for (String attrValue : attrValues)
                if (attrValue.equals(actualAttrValue))
                    return false;

            return true;
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            if (slurpAlignment.checkAlignment(descendants, lastNode)) {
                String actualAttrValue = lastNode.getAttribute(attrQName);

                return actualAttrValue != null && isAttributeValueOutsideExcludedRange(actualAttrValue);
            }

            return false;
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }

    private class RegexValueSlurpAttributeAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final String attrQName;
        private final Pattern valuePattern;

        public RegexValueSlurpAttributeAlignmentWrapper(SlurpAlignment slurpAlignment, String attrQName, Pattern valuePattern) {
            this.slurpAlignment = slurpAlignment;
            this.attrQName = attrQName;
            this.valuePattern = valuePattern;
        }
        @Override
        boolean checkAlignment(int depth, XMLNode lastNode) {
            if (slurpAlignment.checkAlignment(depth, lastNode)) {
                String actualAttrValue = lastNode.getAttribute(attrQName);

                return actualAttrValue != null && valuePattern.matcher(lastNode.getAttribute(attrQName)).find();
            }

            return false;
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants, XMLNode lastNode) {
            if (slurpAlignment.checkAlignment(descendants, lastNode)) {
                String actualAttrValue = lastNode.getAttribute(attrQName);

                return actualAttrValue != null && valuePattern.matcher(actualAttrValue).find();
            }

            return false;
        }

        @Override
        List<String> getPath() {
            return slurpAlignment.getPath();
        }
    }
}
