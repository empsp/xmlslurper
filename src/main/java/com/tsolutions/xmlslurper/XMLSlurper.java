package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.tsolutions.xmlslurper.path.SlurpNode;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Created by mturski on 11/8/2016.
 */
public interface XMLSlurper {
    @NotNull
    SlurpNode getNodes();

    void parse(@NotNull String filepath) throws XMLStreamException, IOException;
}
