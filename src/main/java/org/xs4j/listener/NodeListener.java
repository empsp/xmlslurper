package org.xs4j.listener;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xs4j.XMLNode;

/**
 * Defines an API that allows reading XML document's parsed elements.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface NodeListener {
    /**
     * Implement the following to acquire access to the XML document's parsed elements.
     *
     * @param parent <code>XMLNode</code> of the currently parsed node
     * @param node currently parsed
     */
    void onNode(@Nullable XMLNode parent, @NotNull XMLNode node);
}
