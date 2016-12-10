package org.xs4j;

import java.io.OutputStream;

/**
 * Created by mturski on 12/8/2016.
 */
public interface XMLSpitter {
    XMLStream write(OutputStream outputStream);

    XMLStream write(OutputStream outputStream, String version);

    XMLStream write(OutputStream outputStream, String version, String encoding);
}
