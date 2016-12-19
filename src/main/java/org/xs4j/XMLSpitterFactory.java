package org.xs4j;

import org.xs4j.XMLSpitter.OutputStreamSupplier;

import javax.xml.stream.XMLOutputFactory;
import java.io.OutputStream;

/**
 * Created by mturski on 12/8/2016.
 */
public class XMLSpitterFactory {
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
     * A convenient method to acquire generic {@link OutputStreamSupplier}.
     *
     * @return a new instance of <code>OutputStreamSupplier</code>
     */
    public OutputStreamSupplier createOutputStreamSupplier() {
        return new OutputStreamSupplier() {
            private OutputStream outputStream;

            @Override
            public OutputStream supply() {
                return outputStream;
            }

            @Override
            public OutputStreamSupplier set(OutputStream outputStream) {
                this.outputStream = outputStream;

                return this;
            }
        };
    }
}
