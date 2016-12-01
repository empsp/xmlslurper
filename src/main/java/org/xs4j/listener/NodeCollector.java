package org.xs4j.listener;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xs4j.XMLNode;

import java.util.Collection;

/**
 * Created by mturski on 12/1/2016.
 */
public interface NodeCollector {
    void onCollect(@NotNull Collection<XMLNodeTuple> nodeTuples);

    interface XMLNodeTuple {
        @Nullable
        XMLNode getParent();

        @NotNull
        XMLNode getNode();
    }
}
