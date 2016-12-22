package org.xs4j;

import org.xs4j.listener.NodeListener;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mturski on 11/25/2016.
 */
class NodeNotifier {
    private final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();
    private final List<FindData> findData;
    private final List<FindData> findAllData;

    NodeNotifier(List<FindData> findData, List<FindData> findAllData) {
        this.findData = findData;
        this.findAllData = findAllData;
    }

    void onStartNode(XMLNode node) {
        XMLNodeFactory.setParent(descendants.peekLast(), node);
        descendants.addLast(node);

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
        for (FindData data : findAllData)
            if (data.slurpAlignment.checkAlignment(descendants)) {
                if (data.startNodeListener != null)
                    data.startNodeListener.onNode(node);

                data.alignedNodeIds.add(node.getId());
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
        for (FindData data : findAllData) {
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
        descendants.clear();
        findData.clear();
        findAllData.clear();
    }

    boolean isOnlyFindDataAvailable() {
        return findAllData.isEmpty();
    }

    boolean isFindDataEmpty() {
        return findData.isEmpty();
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
