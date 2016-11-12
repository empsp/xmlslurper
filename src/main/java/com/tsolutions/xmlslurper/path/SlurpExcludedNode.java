package com.tsolutions.xmlslurper.path;

import com.sun.istack.NotNull;

/**
 * Created by mturski on 11/12/2016.
 */
public interface SlurpExcludedNode extends Slurp {
    @NotNull
    SlurpExcludedNode andNotNode(@NotNull String name);
}
