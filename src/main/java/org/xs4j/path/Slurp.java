package org.xs4j.path;

import com.sun.istack.Nullable;
import org.xs4j.listener.NodeListener;

/**
 * Created by mturski on 11/8/2016.
 */
public interface Slurp {
    void find(@Nullable NodeListener nodeListener);

    void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);

    void findAll(@Nullable NodeListener nodeListener);

    void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);
}
