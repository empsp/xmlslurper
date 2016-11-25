package com.tsolutions.xmlslurper;

import com.tsolutions.xmlslurper.listener.NodeListener;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Created by mturski on 11/25/2016.
 */
final class NodeNotifier {
    private Deque<XMLNode> descendants = new ArrayDeque<XMLNode>();
    private List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples;

    NodeNotifier(List<SlurpAlignmentListenerTuple> slurpAlignmentListenerTuples) {
        this.slurpAlignmentListenerTuples = slurpAlignmentListenerTuples;
    }

    void onStartNode(XMLNode child) {
        XMLNode parent = descendants.peekLast();

        for (SlurpAlignmentListenerTuple tuple : slurpAlignmentListenerTuples) {
            NodeListener startNodeListener = tuple.getStartNodeListener();

            if(startNodeListener != null && tuple.getSlurpAlignment().checkAlignment(descendants, child))
                startNodeListener.onNode(parent, child);
        }


        descendants.addLast(child);
    }

    void onEndNode() {
        XMLNode child = descendants.removeLast();
        XMLNode parent = descendants.peekLast();

        for (SlurpAlignmentListenerTuple tuple : slurpAlignmentListenerTuples) {
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
        slurpAlignmentListenerTuples.clear();
    }

    boolean areSingleFindListenersAvailableOnly() {
        return false;
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
