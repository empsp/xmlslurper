package org.xs4j.path;

import org.xs4j.util.NotNull;

/**
 * A search node API that narrows the search to attribute related information.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface SlurpAttribute extends Slurp {
    /**
     * Limits the search to given attribute having given value on the current element.
     *
     * @param value matching attribute's value
     * @return a new instance of <code>Slurp</code> search API to listen for node related events
     */
    @NotNull
    Slurp is(@NotNull String value);

    /**
     * Limits the search to given attribute's value starting with the given value on the current element.
     *
     * @param value that attribute's value starts with
     * @return a new instance of <code>Slurp</code> search API to listen for node related events
     */
    @NotNull
    Slurp startsWith(@NotNull String value);

    /**
     * Limits the search to given attribute having value matching given Regex expression on the current element.
     *
     * @param regex expression to match attribute's value with
     * @return a new instance of <code>Slurp</code> search API to listen for node related events
     */
    @NotNull
    Slurp regex(@NotNull String regex);

    /**
     * Limits the search to given attribute having values other than the given one on the current element.
     *
     * @param value not matching attribute's value
     * @return a new instance of <code>Slurp</code> search API to listen for node related events
     */
    @NotNull
    Slurp isNot(@NotNull String value);

    /**
     * Limits the search to given attribute having values other than the given ones on the current element.
     *
     * @param values not matching attribute's value
     * @return a new instance of <code>Slurp</code> search API to listen for node related events
     */
    @NotNull
    Slurp isNot(@NotNull String... values);
}
