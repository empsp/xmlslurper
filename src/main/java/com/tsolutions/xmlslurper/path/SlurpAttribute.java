package com.tsolutions.xmlslurper.path;

import com.sun.istack.NotNull;

/**
 * Created by mturski on 11/8/2016.
 */
public interface SlurpAttribute extends Slurp {
    @NotNull
    Slurp is(@NotNull String value);

    @NotNull
    Slurp regex(@NotNull String regex);

    @NotNull
    Slurp isNot(@NotNull String value);

    @NotNull
    Slurp isNot(@NotNull String... values);
}
