package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.listener.NodeListener;

import java.util.*;

/**
 * Created by mturski on 11/25/2016.
 */
final class NodeNotifier {
    private final Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();
    private final List<SlurpAlignmentListenerTuple> findTuples;
    private final List<SlurpAlignmentListenerTuple> findAllTuples;

    NodeNotifier(List<SlurpAlignmentListenerTuple> findTuples, List<SlurpAlignmentListenerTuple> findAllTuples) {
        this.findAllTuples = findAllTuples;
        this.findTuples = findTuples;
    }

    void onStartNode(XMLNode child) {
        XMLNode parent = descendants.peekLast();

        for (SlurpAlignmentListenerTuple tuple : findTuples) {
            NodeListener startNodeListener = tuple.getStartNodeListener();

            if(startNodeListener != null && tuple.getSlurpAlignment().checkAlignment(descendants, child)) {
                startNodeListener.onNode(parent, child);
                tuple.removeStartNodeListener();
            }
        }

        for (SlurpAlignmentListenerTuple tuple : findAllTuples) {
            NodeListener startNodeListener = tuple.getStartNodeListener();

            if(startNodeListener != null && tuple.getSlurpAlignment().checkAlignment(descendants, child))
                startNodeListener.onNode(parent, child);
        }


        descendants.addLast(child);
    }

    void onEndNode() {
        XMLNode child = descendants.removeLast();
        XMLNode parent = descendants.peekLast();

        Iterator<SlurpAlignmentListenerTuple> findTuplesIt = findTuples.iterator();
        while(findTuplesIt.hasNext()) {
            SlurpAlignmentListenerTuple tuple = findTuplesIt.next();
            NodeListener endNodeListener = tuple.getEndNodeListener();

            if(endNodeListener != null && tuple.getSlurpAlignment().checkAlignment(descendants, child)) {
                endNodeListener.onNode(parent, child);
                findTuplesIt.remove();
            }
        }

        for (SlurpAlignmentListenerTuple tuple : findAllTuples) {
            NodeListener endNodeListener = tuple.getEndNodeListener();

            if(endNodeListener != null && tuple.getSlurpAlignment().checkAlignment(descendants, child))
                endNodeListener.onNode(parent, child);
        }
    }

    XMLNode peekLastDescendant() {
        return descendants.peekLast();
    }

    void reset() {
        descendants.clear();
        findTuples.clear();
        findAllTuples.clear();
    }

    boolean areSingleFindListenersAvailableOnly() {
        return findAllTuples.isEmpty();
    }

    boolean areSingleFindListenersNotEmpty() {
        return !findTuples.isEmpty();
    }

    static class SlurpAlignmentListenerTuple {
        private final SlurpAlignment slurpAlignment;
        private NodeListener startNodeListener;
        private NodeListener endNodeListener;

        SlurpAlignmentListenerTuple(SlurpAlignment slurpAlignment, NodeListener startNodeListener, NodeListener endNodeListener) {
            this.slurpAlignment = slurpAlignment;
            this.startNodeListener = startNodeListener;
            this.endNodeListener = endNodeListener;
        }

        SlurpAlignmentListenerTuple(SlurpAlignment slurpAlignment, NodeListener nodeListener) {
            this(slurpAlignment, nodeListener, nodeListener);
        }

        SlurpAlignment getSlurpAlignment() {
            return slurpAlignment;
        }

        NodeListener getStartNodeListener() {
            return startNodeListener;
        }

        NodeListener getEndNodeListener() {
            return endNodeListener;
        }

        void removeStartNodeListener() {
            startNodeListener = null;
        }
    }
}
