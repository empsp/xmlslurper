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

    public XMLSpitter createXMLSpitter() {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        return new StAXSpitter(xmlOutputFactory);
    }

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
