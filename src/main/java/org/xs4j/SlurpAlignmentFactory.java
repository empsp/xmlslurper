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

        return new SlurpNthElementAlignmentWrapper(getSlurpAlignment(qNamePath), nodeIndex);
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
        boolean checkAlignment(Deque<XMLNode> descendants) {
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
        boolean checkAlignment(Deque<XMLNode> descendants) {
            return checkAlignment(descendants.size(), descendants.peekLast()); // +1 for lastNode which is a detached part of descendants
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
        boolean checkAlignment(Deque<XMLNode> descendants) {
            Iterator<XMLNode> descIter = descendants.iterator();
            XMLNode descendant;
            String name;

            for (int index = 0; index < namePath.size() && descIter.hasNext(); index++) {
                name = namePath.get(index);
                descendant = descIter.next();

                if (name.equals(DEPTH_MARKER)) {
                    if (index == namePath.size() - 1) // if ** was last in the path, then aligned
                        return true;
                    else if (!descIter.hasNext()) // if descendant was last node and the path is not emptied yet, then not aligned
                        return false;

                    return checkAlignmentTraversingBackwardsUntilIndex(descendants, index);
                } else if ((!name.equals(descendant.getQName()) && !name.equals(SIBLING_MARKER)))
                    break;
            }

            return false;
        }

        private boolean checkAlignmentTraversingBackwardsUntilIndex(Deque<XMLNode> descendants, int namePathForwardTraverseIndex) {
            String name;
            XMLNode descendant;
            boolean isDescendantMode = false;

            Iterator<XMLNode> descIter = descendants.descendingIterator();
            for (int index = namePath.size() - 1; index > namePathForwardTraverseIndex && descIter.hasNext(); index--) { // the path still has an item but the descendants are emptied, then not aligned
                name = namePath.get(index);
                descendant = descIter.next();

                do {
                    if (name.equals(descendant.getQName()) || name.equals(SIBLING_MARKER)) {
                        isDescendantMode = false;
                    } else if (name.equals(DEPTH_MARKER)) {
                        isDescendantMode = true;
                        break;
                    } else if (isDescendantMode) { // names don't match however ** was previously so traverse until found
                        if (!descIter.hasNext()) // names eventually must match but here descendants run out, then not aligned
                            return false;

                        descendant = descIter.next();
                    } else
                        return false;
                } while (isDescendantMode);
            }

            return descIter.hasNext();
        }

        @Override
        public List<String> getPath() {
            return namePath;
        }
    }

    private class SlurpNthElementAlignmentWrapper extends SlurpAlignment {
        private final SlurpAlignment slurpAlignment;
        private final long nodeIndex;

        private Deque<Long> indexByDepth = new ArrayDeque<Long>();
        private int prevDepth;

        public SlurpNthElementAlignmentWrapper(SlurpAlignment slurpAlignment, long nodeIndex) {
            this.slurpAlignment = slurpAlignment;
            this.nodeIndex = nodeIndex;
        }

        @Override
        boolean checkAlignment(int depth, XMLNode lastNode) {
            countOccurences(depth);

            return slurpAlignment.checkAlignment(depth, lastNode) && indexByDepth.peekLast() == nodeIndex;
        }

        @Override
        boolean checkAlignment(Deque<XMLNode> descendants) {
            countOccurences(descendants.size());

            return slurpAlignment.checkAlignment(descendants) && indexByDepth.peekLast() == nodeIndex;
        }

        private void countOccurences(int depth) {
            if (depth > prevDepth)
                indexByDepth.addLast(1L);
            else if (depth == prevDepth)
                indexByDepth.addLast(indexByDepth.removeLast() + 1);
            else {
                indexByDepth.removeLast();

                if (indexByDepth.size() > 0)
                    indexByDepth.addLast(indexByDepth.removeLast() + 1);
            }

            prevDepth = depth;
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
        boolean checkAlignment(Deque<XMLNode> descendants) {
            return slurpAlignment.checkAlignment(descendants) && descendants.peekLast().hasAttribute(attrQName);
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
        boolean checkAlignment(Deque<XMLNode> descendants) {
            return slurpAlignment.checkAlignment(descendants) && attrValue.equals(descendants.peekLast().getAttribute(attrQName));
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
        boolean checkAlignment(Deque<XMLNode> descendants) {
            if (slurpAlignment.checkAlignment(descendants)) {
                String actualAttrValue = descendants.peekLast().getAttribute(attrQName);

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
        boolean checkAlignment(Deque<XMLNode> descendants) {
            if (slurpAlignment.checkAlignment(descendants)) {
                String actualAttrValue = descendants.peekLast().getAttribute(attrQName);

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
