package org.xs4j.xmlslurper;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;
import org.xml.sax.SAXException;
import org.xs4j.xmlslurper.NodeListener;
import org.xs4j.xmlslurper.SlurpNode;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Defines an API for node based XML documents parser.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface XMLSlurper {
    /**
     * Allows {@link NodeListener} instances to be attached to parser in order to retrieve parsed nodes. Simultaneously
     * search pattern can be further extended to include node/attribute names/attribute values. When used without
     * <code>nodes</code> parameter, all nodes are searched by default.
     *
     * @param nodes array that narrows the search to particular XPath/GPath similar pattern
     * @return a search node API that further extends search capabilities
     */
    @NotNull
    SlurpNode getNodes(@Nullable String... nodes);

    /**
     * Triggers the parsing process on the given {@link InputStream}.
     *
     * @param inputStream containing the content to be parsed
     * @throws ParserConfigurationException if a parser cannot be created which satisfies the requested configuration
     * @throws SAXException If parse produces a SAX error.
     * @throws IOException If an IO error occurs interacting with the <code>InputStream</code>.
     */
    void parse(@NotNull InputStream inputStream) throws ParserConfigurationException, SAXException, IOException;

    /**
     * Triggers the parsing process on the given {@link InputStream} with the given {@link Schema}.
     *
     * @param inputStream containing the content to be parsed
     * @param schemaFile <code>Schema</code> to be used for XML document validation purposes
     * @throws ParserConfigurationException if a parser cannot be created which satisfies the requested configuration
     * @throws SAXException If parse produces a SAX error.
     * @throws IOException If an IO error occurs interacting with the <code>InputStream</code>.
     */
    void parse(@NotNull InputStream inputStream, @NotNull File schemaFile) throws ParserConfigurationException, SAXException, IOException;
}
