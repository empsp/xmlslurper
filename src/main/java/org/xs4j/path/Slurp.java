package org.xs4j.path;

import org.xs4j.util.Nullable;
import org.xs4j.listener.NodeListener;

/**
 * Base API for node/attribute name/attribute value searching. Provides methods to attach {@link NodeListener} instances
 * and hence retrieve required nodes. Allows searching for single occurrence/multiple occurrences of the required nodes.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface Slurp {
    /**
     * Upon finding first element matching the search pattern, the following will signal and provide relevant node
     * information to the given {@link NodeListener} instance. The parsing will end if all listeners are granted with
     * the data and there are no listeners to the {@link Slurp#findAll} events. Typically, it will be used as anonymous
     * class relaying important information to the outside logic.
     *
     * @param nodeListener to provide data to
     */
    void find(@Nullable NodeListener nodeListener);

    /**
     * Upon finding first element matching the search pattern, the following will signal and provide relevant node
     * information to the given {@link NodeListener} instances. Depending on which parameter supplies the listeners,
     * upon start-tag/end-tag the signaling will occur. The parsing will end if all listeners are granted with the data
     * and there are no listeners to the {@link Slurp#findAll} events. Typically, it will be used as anonymous class
     * relaying important information to the outside logic.
     *
     * @param startNodeListener to provide start-tag related element data to
     * @param endNodeListener to provide end-tag related element data to
     */
    void find(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);

    /**
     * Upon finding an element matching the search pattern, the following will signal and provide relevant node
     * information to the given {@link NodeListener} instance. The parsing will end on end of file (even if there are
     * listeners to {@link Slurp#find} events). Typically, it will be used as anonymous class relaying important
     * information to the outside logic.
     *
     * @param nodeListener to provide data to
     */
    void findAll(@Nullable NodeListener nodeListener);

    /**
     * Upon finding an element matching the search pattern, the following will signal and provide relevant node
     * information to the given {@link NodeListener} instances. Depending on which parameter supplies the listeners,
     * upon start-tag/end-tag the signaling will occur. The parsing will end on end of file (even if there are
     * listeners to {@link Slurp#find} events). Typically, it will be used as anonymous class relaying important
     * information to the outside logic.
     *
     * @param startNodeListener to provide start-tag related element data to
     * @param endNodeListener to provide end-tag related element data to
     */
    void findAll(@Nullable NodeListener startNodeListener, @Nullable NodeListener endNodeListener);
}
