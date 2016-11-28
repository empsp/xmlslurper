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

    void onStartNode(XMLNode child) {
        XMLNode parent = descendants.peekLast();

        notifyFindListenersOnStartNode(parent, child);
        notifyFindAllListenersOnStartNode(parent, child);

        descendants.addLast(child);
    }

    private void notifyFindListenersOnStartNode(XMLNode parent, XMLNode child) {
        Iterator<NodeNotifierData> findDataIt = findData.iterator();

        while(findDataIt.hasNext()) {
            NodeNotifierData data = findDataIt.next();

            if (data.nodeIdFoundDuringOnStart == NodeNotifierData.NULL && data.slurpAlignment.checkAlignment(descendants, child)) {
                data.nodeIdFoundDuringOnStart = child.getId();

                if (data.startNodeListener != null) {
                    data.startNodeListener.onNode(parent, child);
                    data.startNodeListener = null;

                    if (data.endNodeListener == null)
                        findDataIt.remove();
                }
            }
        }
    }

    private void notifyFindAllListenersOnStartNode(XMLNode parent, XMLNode child) {
        for (NodeNotifierData data : findAllData)
            if (data.startNodeListener != null && data.slurpAlignment.checkAlignment(descendants, child))
                data.startNodeListener.onNode(parent, child);
    }

    void onEndNode() {
        XMLNode child = descendants.removeLast();
        XMLNode parent = descendants.peekLast();

        notifyFindListenersOnEndNode(parent, child);
        notifyFindAllListenersOnEndNode(parent, child);
    }

    private void notifyFindListenersOnEndNode(XMLNode parent, XMLNode child) {
        Iterator<NodeNotifierData> findDataIt = findData.iterator();

        while(findDataIt.hasNext()) {
            NodeNotifierData data = findDataIt.next();

            if (data.nodeIdFoundDuringOnStart == child.getId()) {
                if (data.endNodeListener != null)
                    data.endNodeListener.onNode(parent, child);

                findDataIt.remove();
            }
        }
    }

    private void notifyFindAllListenersOnEndNode(XMLNode parent, XMLNode child) {
        for (NodeNotifierData data : findAllData)
            if (data.endNodeListener != null && data.slurpAlignment.checkAlignment(descendants, child))
                data.endNodeListener.onNode(parent, child);
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
        private static final long NULL = -1;

        private SlurpAlignment slurpAlignment;
        private NodeListener startNodeListener;
        private NodeListener endNodeListener;

        private long nodeIdFoundDuringOnStart = NULL;

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
