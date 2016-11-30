package org.xs4j.listener;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xs4j.XMLNode;

/**
 * Created by mturski on 11/11/2016.
 */
public interface NodeListener {
    void onNode(@Nullable XMLNode parent, @NotNull XMLNode node);
}
