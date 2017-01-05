package org.xs4j;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Created by mturski on 12/9/2016.
 */
public class XMLStreamRuntimeException extends RuntimeException {
    public XMLStreamRuntimeException(String message) {
        super(message);
    }

    public XMLStreamRuntimeException(XMLStreamException e) {
        super(e.getMessage(), e.getCause());
    }

    public XMLStreamRuntimeException(IOException e) {
        super(e.getMessage(), e.getCause());
    }
}
