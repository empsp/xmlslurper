package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

/**
 * Created by mturski on 11/11/2016.
 */
public interface SlurpListener {
    void onNode(@Nullable XMLNode parent, @NotNull XMLNode node);
}
