package org.xs4j;

import org.xs4j.path.Slurp;
import org.xs4j.util.NotNull;

import java.io.OutputStream;

/**
 * Defines a provider/wrapper API for node based XML documents writer.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface XMLSpitter {
    /**
     * Writes single document node and it's content as a new XML document. Upon end writing, underlying
     * {@link OutputStream} will be closed automatically.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputStreamSupplier supplier of the {@link OutputStream}
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputStreamSupplier outputStreamSupplier);

    /**
     * Writes single document node and it's content as a new XML document. Upon end writing, underlying
     * {@link OutputStream} will be closed automatically.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputStreamSupplier supplier of the {@link OutputStream}
     * @param version new document's XML version
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputStreamSupplier outputStreamSupplier, @NotNull final String version);

    /**
     * Writes single document node and it's content as a new XML document. Upon end writing, underlying
     * {@link OutputStream} will be closed automatically.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputStreamSupplier supplier of the {@link OutputStream}
     * @param version new document's XML version
     * @param encoding new document's XML encoding
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void write(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputStreamSupplier outputStreamSupplier, @NotNull final String version, @NotNull final String encoding);

    /**
     * Writes all document nodes matching {@link Slurp} <code>documentNode</code> parameter as new XML documents. Please
     * remember to provide new instance of {@link OutputStream} via {@link OutputStreamSupplier} each time new
     * documentNode is detected, otherwise {@link XMLStreamRuntimeException} will be thrown as the {@link OutputStream}
     * is automatically closed upon end writing.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputStreamSupplier supplier of the {@link OutputStream}
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputStreamSupplier outputStreamSupplier);

    /**
     * Writes all document nodes matching {@link Slurp} <code>documentNode</code> parameter as new XML documents. Please
     * remember to provide new instance of {@link OutputStream} via {@link OutputStreamSupplier} each time new
     * documentNode is detected, otherwise {@link XMLStreamRuntimeException} will be thrown as the {@link OutputStream}
     * is automatically closed upon end writing.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputStreamSupplier supplier of the {@link OutputStream}
     * @param version new document's XML version
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputStreamSupplier outputStreamSupplier, @NotNull final String version);

    /**
     * Writes all document nodes matching {@link Slurp} <code>documentNode</code> parameter as new XML documents. Please
     * remember to provide new instance of {@link OutputStream} via {@link OutputStreamSupplier} each time new
     * documentNode is detected, otherwise {@link XMLStreamRuntimeException} will be thrown as the {@link OutputStream}
     * is automatically closed upon end writing.
     *
     * @param documentNode <code>Slurp</code> identifying parsed input XML document element that will act as root
     *                     element in a new document
     * @param contentNodes <code>Slurp</code> identifying parsed input XML document elements that will be included as a
     *                     content into a new document
     * @param outputStreamSupplier supplier of the {@link OutputStream}
     * @param version new document's XML version
     * @param encoding new document's XML encoding
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    void writeAll(@NotNull Slurp documentNode, @NotNull Slurp contentNodes, @NotNull final OutputStreamSupplier outputStreamSupplier, @NotNull final String version, @NotNull final String encoding);

    /**
     * Creates a new instance of {@link XMLStream} that does the actual XML document write. <code>XMLStream</code>
     * provides more freedom in new document construction. It allows creating XML documents from scratch without the
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
     * provides more freedom in new document construction. It allows creating XML documents from scratch without the
     * need to previously parse any input XML documents.
     *
     * @param outputStream the stream to which the document will be written
     * @param version new document's XML version
     * @return a new instance of <code>XMLStream</code> for writing XML documents
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    @NotNull
    XMLStream createStream(@NotNull OutputStream outputStream, @NotNull String version);

    /**
     * Creates a new instance of {@link XMLStream} that does the actual XML document write. <code>XMLStream</code>
     * provides more freedom in new document construction. It allows creating XML documents from scratch without the
     * need to previously parse any input XML documents.
     *
     * @param outputStream the stream to which the document will be written
     * @param version new document's XML version
     * @param encoding new document's XML encoding
     * @return a new instance of <code>XMLStream</code> for writing XML documents
     * @throws XMLStreamRuntimeException wrapped <code>RuntimeException</code> of <code>XMLStreamException</code>
     */
    @NotNull
    XMLStream createStream(@NotNull OutputStream outputStream, @NotNull String version, @NotNull String encoding);

    /**
     * A data structure holding information about an {@link OutputStream}. The following is mutable to allow for dynamic
     * switches of the streams for multiple writing events.
     */
    interface OutputStreamSupplier {
        /**
         * Used internally to supply {@link XMLSpitter} with {@link OutputStream}.
         *
         * @return a stream to which the document will be written
         */
        @NotNull
        OutputStream supply();

        /**
         * Sets the {@link OutputStream} to be used for new XML document writing.
         *
         * @param outputStream
         * @return this instance of <code>OutputStreamSupplier</code>. Used for convenience for method chaining.
         */
        OutputStreamSupplier set(@NotNull OutputStream outputStream);
    }
}
