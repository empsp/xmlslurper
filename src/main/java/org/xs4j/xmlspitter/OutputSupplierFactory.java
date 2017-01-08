package org.xs4j.xmlspitter;

import org.xs4j.util.NotNull;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Created by mturski on 1/5/2017.
 */
public class OutputSupplierFactory {
    public static OutputSupplierFactory getInstance() {
        return new OutputSupplierFactory();
    }

    private OutputSupplierFactory() {
    }

    /**
     * A convenient method to acquire generic {@link OutputSupplier}.
     *
     * @param <T> type of output
     * @return a new instance of <code>OutputSupplier</code>
     */
    public <T> OutputSupplier<T> createOutputSupplier() {
        return new GenericOutputSupplier<T>();
    }

    /**
     * A convenient method to acquire generic {@link OutputSupplier}.
     *
     * @param clazz instance of declared type of output
     * @param <T> type of output
     * @return a new instance of <code>OutputSupplier</code>
     */
    public <T> OutputSupplier<T> createOutputSupplier(Class<T> clazz) {
        return new GenericOutputSupplier<T>();
    }

    class GenericOutputSupplier<T> implements OutputSupplier<T> {
        static final String ILLEGAL_SUPPLIER_ARGUMENT = "%s must supply with object instances of %s or %s";

        private T output;

        @Override
        public T supply() {
            return output;
        }

        @Override
        public OutputSupplier<T> set(@NotNull T output) {
            if (!(output instanceof Writer) && !(output instanceof OutputStream) )
                throw new IllegalArgumentException(String.format(ILLEGAL_SUPPLIER_ARGUMENT, OutputSupplier.class.getName(), Writer.class.getName(), OutputStream.class.getName()));

            this.output = output;

            return this;
        }
    }
}
