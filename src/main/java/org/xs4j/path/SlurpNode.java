package org.xs4j.path;

import com.sun.istack.NotNull;

/**
 *
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface SlurpNode extends Slurp {
    @NotNull
    SlurpNode node(@NotNull String qName);

    @NotNull
    SlurpNode get(long nodeIndex);

    @NotNull
    SlurpAttribute attr(@NotNull String qName);
}
