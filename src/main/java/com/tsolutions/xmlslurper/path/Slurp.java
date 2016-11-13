package com.tsolutions.xmlslurper.path;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.SlurpListener;

/**
 * Created by mturski on 11/8/2016.
 */
public interface Slurp {
    void find(@NotNull SlurpListener slurpListener);
}
