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
    private final Deque<FindData> findData;
    private final Deque<FindData> findAllData;
    private final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();

    NodeNotifier(PositionCounter positionCounter, Deque<FindData> findData, Deque<FindData> findAllData) {
        this.positionCounter = positionCounter;
        this.findData = findData;
        this.findAllData = findAllData;
    }

    void onStartNode(XMLNode node) {
        XMLNodeFactory.setParent(descendants.peekLast(), node);
        descendants.addLast(node);
        XMLNodeFactory.setPosition(node, positionCounter.getNodePosition(descendants.size()));

        notifyFindListenersOnStartNode(node);
        notifyFindAllListenersOnStartNode(node);
    }

    private void notifyFindListenersOnStartNode(XMLNode node) {
        Iterator<FindData> findDataIt = findData.iterator();

        while(findDataIt.hasNext()) {
            FindData data = findDataIt.next();

            if (data.alignedNodeIds.isEmpty() && data.slurpAlignment.checkAlignment(descendants)) {
                data.alignedNodeIds.addLast(node.getId());

                if (data.startNodeListener != null) {
                    data.startNodeListener.onNode(node);
                    data.startNodeListener = null;

                    if (data.endNodeListener == null)
                        findDataIt.remove();
                }
            }
        }
    }

    private void notifyFindAllListenersOnStartNode(XMLNode node) {
        Iterator<FindData> findAllDataIt = findAllData.iterator();

        while(findAllDataIt.hasNext()) {
            FindData data = findAllDataIt.next();

            if (data.slurpAlignment.checkAlignment(descendants)) {
                if (data.startNodeListener != null)
                    data.startNodeListener.onNode(node);

                data.alignedNodeIds.add(node.getId());
            }
        }
    }

    void onEndNode() {
        XMLNode node = descendants.removeLast();

        notifyFindListenersOnEndNode(node);
        notifyFindAllListenersOnEndNode(node);
    }

    private void notifyFindListenersOnEndNode(XMLNode node) {
        Iterator<FindData> findDataIt = findData.iterator();

        while(findDataIt.hasNext()) {
            FindData data = findDataIt.next();

            if (data.alignedNodeIds.contains(node.getId())) {
                if (data.endNodeListener != null)
                    data.endNodeListener.onNode(node);

                findDataIt.remove();
            }
        }
    }

    private void notifyFindAllListenersOnEndNode(XMLNode node) {
        Iterator<FindData> findAllDataIt = findAllData.iterator();

        while(findAllDataIt.hasNext()) {
            FindData data = findAllDataIt.next();
            long nodeId = node.getId();

            if (data.alignedNodeIds.contains(nodeId)) {
                if (data.endNodeListener != null)
                    data.endNodeListener.onNode(node);

                data.alignedNodeIds.remove(nodeId);
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
        findAllData.clear();
    }

    boolean isFindDataEmpty() {
        return findData.isEmpty();
    }

    public boolean isFindAllDataEmpty() {
        return findAllData.isEmpty();
    }

    static class FindData {
        private SlurpAlignment slurpAlignment;
        private NodeListener startNodeListener;
        private NodeListener endNodeListener;

        private Deque<Long> alignedNodeIds = new ArrayDeque<Long>();

        FindData(SlurpAlignment slurpAlignment, NodeListener startNodeListener, NodeListener endNodeListener) {
            this.slurpAlignment = slurpAlignment;
            this.startNodeListener = startNodeListener;
            this.endNodeListener = endNodeListener;
        }

        FindData(SlurpAlignment slurpAlignment, NodeListener nodeListener) {
            this(slurpAlignment, nodeListener, nodeListener);
        }
    }
}
