package com.tsolutions.xmlslurper.listener;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.tsolutions.xmlslurper.XMLNode;

/**
 * Created by mturski on 11/11/2016.
 */
public interface NodeListener {
    void onNode(@Nullable XMLNode parent, @NotNull XMLNode node);
}
