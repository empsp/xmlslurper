package com.tsolutions.xmlslurper.path;

import com.sun.istack.Nullable;
import com.tsolutions.xmlslurper.listener.NodeListener;

/**
 * Created by mturski on 11/8/2016.
 */
public interface Slurp {
    void find(@Nullable NodeListener nodeListener);

    void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);

    void findAll(@Nullable NodeListener nodeListener);

    void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);
}
