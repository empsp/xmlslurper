package com.tsolutions.xmlslurper.path;

import com.sun.istack.NotNull;

/**
 * Created by mturski on 11/8/2016.
 */
public interface SlurpNode extends Slurp {
    @NotNull
    SlurpNode node(@NotNull String name);

    @NotNull
    SlurpExcludedNode notNode(@NotNull String name);

    @NotNull
    SlurpAttribute attr(@NotNull String name);
}
