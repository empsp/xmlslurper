package org.xs4j.xmlspitter;

import org.xs4j.xmlslurper.Slurp;
import org.xs4j.util.NotNull;

import java.io.OutputStream;
import java.io.Writer;

/**
 * Defines a provider/wrapper API for node based XML documents writer.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface XMLSpitter {
    /**
     * Writes single document node and it's content as a new XML document. Upon end writing, underlying output source
     * will be closed automatically.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputSupplier supplier of the {@link OutputStream} or {@link Writer}
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier);

    /**
     * Writes single document node and it's content as a new XML document. Upon end writing, underlying output source
     * will be closed automatically.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputSupplier supplier of the {@link OutputStream} or {@link Writer}
     * @param version new document's XML version
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, @NotNull final String version);

    /**
     * Writes single document node and it's content as a new XML document. Upon end writing, underlying output source
     * will be closed automatically.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputSupplier supplier of the {@link OutputStream} or {@link Writer}
     * @param version new document's XML version
     * @param encoding new document's XML encoding
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, @NotNull final String version, @NotNull final String encoding);

    /**
     * Writes all document nodes matching {@link Slurp} <code>documentNode</code> parameter as new XML documents. Please
     * remember to provide new instance of output source via {@link OutputSupplier} each time new documentNode is
     * detected, otherwise {@link XMLStreamRuntimeException} will be thrown as the output source is automatically closed
     * upon end of writing.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputSupplier supplier of the {@link OutputStream} or {@link Writer}
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier);

    /**
     * Writes all document nodes matching {@link Slurp} <code>documentNode</code> parameter as new XML documents. Please
     * remember to provide new instance of output source via {@link OutputSupplier} each time new documentNode is
     * detected, otherwise {@link XMLStreamRuntimeException} will be thrown as the output source is automatically closed
     * upon end of writing.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputSupplier supplier of the {@link OutputStream} or {@link Writer}
     * @param version new document's XML version
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, @NotNull final String version);

    /**
     * Writes all document nodes matching {@link Slurp} <code>documentNode</code> parameter as new XML documents. Please
     * remember to provide new instance of output source via {@link OutputSupplier} each time new documentNode is
     * detected, otherwise {@link XMLStreamRuntimeException} will be thrown as the output source is automatically closed
     * upon end of writing.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputSupplier supplier of the {@link OutputStream} or {@link Writer}
     * @param version new document's XML version
     * @param encoding new document's XML encoding
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputSupplier<?> outputSupplier, @NotNull final String version, @NotNull final String encoding);

    /**
     * Creates a new instance of {@link XMLStream} that does the actual XML document write. <code>XMLStream</code>
     * provides more freedom in XML document construction. It allows creating XML documents from scratch without the
     * need to previously parse any input XML documents.
     *
     * @param outputStream the stream to which the document will be written
     * @return a new instance of <code>XMLStream</code> for writing XML documents
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    @NotNull
    XMLStream createStream(@NotNull OutputStream outputStream);

    /**
     * Creates a new instance of {@link XMLStream} that does the actual XML document write. <code>XMLStream</code>
     * provides more freedom in XML document construction. It allows creating XML documents from scratch without the
     * need to previously parse any input XML documents.
     *
     * @param writer to which the document will be written
     * @return a new instance of <code>XMLStream</code> for writing XML documents
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    @NotNull
    XMLStream createStream(@NotNull Writer writer);
}
