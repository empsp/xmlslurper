package org.xs4j.path;

import com.sun.istack.NotNull;

/**
 *
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface SlurpAttribute extends Slurp {
    @NotNull
    Slurp is(@NotNull String value);

    @NotNull
    Slurp regex(@NotNull String regex);

    @NotNull
    Slurp isNot(@NotNull String value);

    @NotNull
    Slurp isNot(@NotNull String... values);
}
