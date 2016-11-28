package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.listener.NodeListener;

import java.util.*;

/**
 * Created by mturski on 11/25/2016.
 */
final class NodeNotifier {
    private final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();
    private final List<NodeNotifierData> findData;
    private final List<NodeNotifierData> findAllData;

    NodeNotifier(List<NodeNotifierData> findData, List<NodeNotifierData> findAllData) {
        this.findData = findData;
        this.findAllData = findAllData;
    }

    void onStartNode(XMLNode node) {
        XMLNode parent = descendants.peekLast();

        notifyFindListenersOnStartNode(parent, node);
        notifyFindAllListenersOnStartNode(parent, node);

        descendants.addLast(node);
    }

    private void notifyFindListenersOnStartNode(XMLNode parent, XMLNode node) {
        Iterator<NodeNotifierData> findDataIt = findData.iterator();

        while(findDataIt.hasNext()) {
            NodeNotifierData data = findDataIt.next();

            if (data.alignedNodeIds.isEmpty() && data.slurpAlignment.checkAlignment(descendants, node)) {
                data.alignedNodeIds.addLast(node.getId());

                if (data.startNodeListener != null) {
                    data.startNodeListener.onNode(parent, node);
                    data.startNodeListener = null;

                    if (data.endNodeListener == null)
                        findDataIt.remove();
                }
            }
        }
    }

    private void notifyFindAllListenersOnStartNode(XMLNode parent, XMLNode node) {
        for (NodeNotifierData data : findAllData)
            if (data.slurpAlignment.checkAlignment(descendants, node)) {
                if (data.startNodeListener != null)
                    data.startNodeListener.onNode(parent, node);

                data.alignedNodeIds.add(node.getId());
            }
    }

    void onEndNode() {
        XMLNode node = descendants.removeLast();
        XMLNode parent = descendants.peekLast();

        notifyFindListenersOnEndNode(parent, node);
        notifyFindAllListenersOnEndNode(parent, node);
    }

    private void notifyFindListenersOnEndNode(XMLNode parent, XMLNode node) {
        Iterator<NodeNotifierData> findDataIt = findData.iterator();

        while(findDataIt.hasNext()) {
            NodeNotifierData data = findDataIt.next();

            if (data.alignedNodeIds.contains(node.getId())) {
                if (data.endNodeListener != null)
                    data.endNodeListener.onNode(parent, node);

                findDataIt.remove();
            }
        }
    }

    private void notifyFindAllListenersOnEndNode(XMLNode parent, XMLNode node) {
        for (NodeNotifierData data : findAllData) {
            long nodeId = node.getId();

            if (data.alignedNodeIds.contains(nodeId)) {
                if (data.endNodeListener != null)
                    data.endNodeListener.onNode(parent, node);

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

    boolean areSingleFindListenersAvailableOnly() {
        return findAllData.isEmpty();
    }

    boolean areSingleFindListenersEmpty() {
        return findData.isEmpty();
    }

    static class NodeNotifierData {
        private SlurpAlignment slurpAlignment;
        private NodeListener startNodeListener;
        private NodeListener endNodeListener;

        private Deque<Long> alignedNodeIds = new ArrayDeque<Long>();

        NodeNotifierData(SlurpAlignment slurpAlignment, NodeListener startNodeListener, NodeListener endNodeListener) {
            this.slurpAlignment = slurpAlignment;
            this.startNodeListener = startNodeListener;
            this.endNodeListener = endNodeListener;
        }

        NodeNotifierData(SlurpAlignment slurpAlignment, NodeListener nodeListener) {
            this(slurpAlignment, nodeListener, nodeListener);
        }
    }
}
