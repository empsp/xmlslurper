package org.xs4j.xmlslurper;

import org.xs4j.XMLNode;
import org.xs4j.util.NotNull;

/**
 * Defines an API that allows reading XML document's parsed elements.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface NodeListener {
    /**
     * Implement the following to acquire access to the XML document's parsed elements.
     *
     * @param node currently parsed
     */
    void onNode(@NotNull XMLNode node);
}
