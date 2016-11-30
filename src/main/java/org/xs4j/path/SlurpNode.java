package org.xs4j.path;

import com.sun.istack.NotNull;

/**
 * Created by mturski on 11/8/2016.
 */
public interface SlurpNode extends Slurp {
    @NotNull
    SlurpNode node(@NotNull String qName);

    @NotNull
    SlurpAttribute attr(@NotNull String qName);
}
