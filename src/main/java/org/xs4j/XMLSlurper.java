package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xml.sax.SAXException;
import org.xs4j.path.SlurpNode;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mturski on 11/8/2016.
 */
public interface XMLSlurper {
    @NotNull
    SlurpNode getNodes(@Nullable String... nodes);

    void parse(@NotNull InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, XMLStreamException;

    void parse(@NotNull InputStream inputStream, @NotNull File schemaFile) throws ParserConfigurationException, SAXException, IOException, XMLStreamException;
}
