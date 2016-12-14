package org.xs4j;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;

/**
 * Created by mturski on 12/9/2016.
 */
public class XMLStreamRuntimeException extends RuntimeException {
    XMLStreamRuntimeException(XMLStreamException e) {
        super(e);
    }

    XMLStreamRuntimeException(FileNotFoundException e) {
        super(e);
    }
}
