package org.xs4j;

import org.xs4j.util.NotNull;

/**
 * A data structure holding an output source for the {@link XMLSpitter}. The following is mutable to enable
 * setting/changing the output source during runtime.
 */
public interface OutputSupplier<T> {
    /**
     * Used internally to supply {@link XMLSpitter} with output.
     *
     * @return an output to which the document will be written
     */
    @NotNull
    T supply();

    /**
     * Sets the output to be used for new XML document writing.
     *
     * @param output being the supply
     * @return this instance of <code>OutputSupplier</code>. Used for convenience for method chaining.
     */
    OutputSupplier<T> set(@NotNull T output);
}
