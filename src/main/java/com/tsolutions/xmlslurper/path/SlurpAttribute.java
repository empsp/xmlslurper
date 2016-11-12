package com.tsolutions.xmlslurper.path;

import com.sun.istack.NotNull;

/**
 * Created by mturski on 11/8/2016.
 */
public interface SlurpAttribute extends Slurp {
    @NotNull
    SlurpAttribute is(@NotNull String value);

    @NotNull
    SlurpAttribute isNot(@NotNull String value);
}
