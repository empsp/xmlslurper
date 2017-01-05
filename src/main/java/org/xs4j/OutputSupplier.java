package org.xs4j;

import org.xs4j.util.NotNull;

import java.io.OutputStream;

/**
 * A data structure holding information about an {@link OutputStream}. The following is mutable to allow for dynamic
 * switches of the streams for multiple writing events.
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
