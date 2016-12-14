package org.xs4j.path;

import com.sun.istack.Nullable;
import org.xs4j.listener.NodeListener;

/**
 * Base API for node/attribute name/attribute value searching. Provides methods to attach {@link NodeListener} objects
 * and hence retrieve required nodes. Allows searching for single occurrence/multiple occurrences of required nodes.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface Slurp {
    void find(@Nullable NodeListener nodeListener);

    void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);

    void findAll(@Nullable NodeListener nodeListener);

    void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);
}
