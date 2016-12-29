package org.xs4j;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Created by mturski on 12/9/2016.
 */
public class XMLStreamRuntimeException extends RuntimeException {
    XMLStreamRuntimeException(XMLStreamException e) {
        super(e);
    }

    XMLStreamRuntimeException(IOException e) {
        super(e);
    }
}
