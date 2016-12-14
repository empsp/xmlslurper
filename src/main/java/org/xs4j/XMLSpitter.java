package org.xs4j;

import org.xs4j.path.Slurp;

import java.io.OutputStream;

/**
 * Created by mturski on 12/8/2016.
 */
public interface XMLSpitter {
    XMLStream write(OutputStream outputStream);

    XMLStream write(OutputStream outputStream, String version);

    XMLStream write(OutputStream outputStream, String version, String encoding);

    void split(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier);

    void split(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version);

    void split(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version, final String encoding);

    void splitAll(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier);

    void splitAll(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version);

    void splitAll(Slurp documentNode, Slurp contentNodes, final OutputStreamSupplier outputStreamSupplier, final String version, final String encoding);

    interface OutputStreamSupplier {
        OutputStream supply();

        OutputStreamSupplier set(OutputStream os);
    }
}
