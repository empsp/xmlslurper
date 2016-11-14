package com.tsolutions.xmlslurper.path;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.listener.SlurpListener;

/**
 * Created by mturski on 11/8/2016.
 */
public interface Slurp {
    void findAll(@NotNull SlurpListener slurpListener);
}
