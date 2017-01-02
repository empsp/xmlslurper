package org.xs4j;

import org.xs4j.XMLSpitter.OutputSupplier;
import org.xs4j.util.NotNull;

import javax.xml.stream.XMLOutputFactory;

import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 12/8/2016.
 */
public class XMLSpitterFactory {
    public static final String DEFAULT_XML_DOCUMENT_VERSION = "1.0";
    public static final String DEFAULT_XML_DOCUMENT_ENCODING = "UTF-8";

    public static XMLSpitterFactory getInstance() {
        return new XMLSpitterFactory();
    }

    private XMLSpitterFactory() {
    }

    /**
     * Returns a new instance of {@link XMLSpitter}
     *
     * @return a new instance of <code>XMLSpitter</code>
     */
    public XMLSpitter createXMLSpitter() {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        return new StAXSpitter(xmlOutputFactory);
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

    public static class GenericOutputSupplier<T> implements OutputSupplier<T> {
        private T output;

        @Override
        public T supply() {
            return output;
        }

        @Override
        public OutputSupplier<T> set(@NotNull T output) {
            requireNonNull(output);

            this.output = output;

            return this;
        }
    }
}
