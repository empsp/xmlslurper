package org.xs4j;

import org.xs4j.listener.NodeListener;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Created by mturski on 11/25/2016.
 */
class NodeNotifier {
    private final PositionCounter positionCounter;
    private final Deque<FindData> findData = new ArrayDeque<FindData>();
    private final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

    private long findOneDataCounter;

    NodeNotifier(PositionCounter positionCounter) {
        this.positionCounter = positionCounter;
    }

    void onStartNode(XMLNode node) {
        XMLNodeFactory.setPositionalData(node, descendants.peekLast(), positionCounter.getNodePosition(descendants.size() + 1), descendants.size() + 1);
        descendants.addLast(node);

        Iterator<FindData> findDataIt = findData.iterator();
        while(findDataIt.hasNext()) {
            FindData data = findDataIt.next();

            if (data.ifAlignedNotifyStartNodeListenerReturnTrueForRemoval(descendants, node)) { // true only for FindOneData
                findDataIt.remove();
                findOneDataCounter--;
            }
        }
    }

    void onEndNode() {
        XMLNode node = descendants.removeLast();

        Iterator<FindData> findDataIt = findData.iterator();
        while(findDataIt.hasNext()) {
            FindData data = findDataIt.next();

            if (data.ifAlignedNotifyEndNodeListenerReturnTrueForRemoval(node)) { // true only for FindOneData
                findDataIt.remove();
                findOneDataCounter--;
            }
        }
    }

    XMLNode peekLastDescendant() {
        return descendants.peekLast();
    }

    void reset() {
        positionCounter.reset();
        descendants.clear();
        findData.clear();
        findOneDataCounter = 0L;
    }

    boolean isFindDataEmpty() {
        return findOneDataCounter == 0L;
    }

    boolean isFindAllDataEmpty() {
        return findData.isEmpty();
    }

    void addFindData(FindOneData data) {
        findOneDataCounter++;
        findData.addLast(data);
    }

    void addFindData(FindAllData data) {
        findData.addLast(data);
    }

    abstract static class FindData {
        SlurpAlignment slurpAlignment;
        NodeListener startNodeListener;
        NodeListener endNodeListener;

        Deque<Long> alignedNodeIds = new ArrayDeque<Long>();

        FindData(SlurpAlignment slurpAlignment, NodeListener startNodeListener, NodeListener endNodeListener) {
            this.slurpAlignment = slurpAlignment;
            this.startNodeListener = startNodeListener;
            this.endNodeListener = endNodeListener;
        }

        abstract boolean ifAlignedNotifyStartNodeListenerReturnTrueForRemoval(Deque<XMLNode> descendants, XMLNode node);

        abstract boolean ifAlignedNotifyEndNodeListenerReturnTrueForRemoval(XMLNode node);
    }

    static class FindOneData extends FindData {
        FindOneData(SlurpAlignment slurpAlignment, NodeListener startNodeListener, NodeListener endNodeListener) {
            super(slurpAlignment, startNodeListener, endNodeListener);
        }

        boolean ifAlignedNotifyStartNodeListenerReturnTrueForRemoval(Deque<XMLNode> descendants, XMLNode node) {
            if (alignedNodeIds.isEmpty() && slurpAlignment.checkAlignment(descendants)) {
                if (startNodeListener != null) {
                    startNodeListener.onNode(node);
                    startNodeListener = null;
                }

                alignedNodeIds.addLast(node.getId());
                
                return endNodeListener == null;
            }

            return false;
        }

        @Override
        boolean ifAlignedNotifyEndNodeListenerReturnTrueForRemoval(XMLNode node) {
            if (alignedNodeIds.contains(node.getId())) {
                if (endNodeListener != null)
                    endNodeListener.onNode(node);

                return true;
            }

            return false;
        }
    }

    static class FindAllData extends FindData {
        FindAllData(SlurpAlignment slurpAlignment, NodeListener startNodeListener, NodeListener endNodeListener) {
            super(slurpAlignment, startNodeListener, endNodeListener);
        }

        boolean ifAlignedNotifyStartNodeListenerReturnTrueForRemoval(Deque<XMLNode> descendants, XMLNode node) {
            if (slurpAlignment.checkAlignment(descendants)) {
                if (startNodeListener != null)
                    startNodeListener.onNode(node);

                alignedNodeIds.addLast(node.getId());
            }

            return false;
        }

        @Override
        boolean ifAlignedNotifyEndNodeListenerReturnTrueForRemoval(XMLNode node) {
            long nodeId = node.getId();

            if (alignedNodeIds.contains(nodeId)) {
                if (endNodeListener != null)
                    endNodeListener.onNode(node);

                alignedNodeIds.remove(nodeId);
            }

            return false;
        }
    }
}
