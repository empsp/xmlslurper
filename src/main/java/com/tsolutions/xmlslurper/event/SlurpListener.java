package com.tsolutions.xmlslurper.event;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.tsolutions.xmlslurper.path.Node;

/**
 * Created by mturski on 11/11/2016.
 */
public interface SlurpListener {
    void onNode(@Nullable Node parent, @NotNull Node node);
}
