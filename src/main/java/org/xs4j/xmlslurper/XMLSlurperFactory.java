package org.xs4j.xmlslurper;

import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;
import org.xs4j.xmlslurper.SAXSlurper.*;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xs4j.XMLNodeFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

/**
 * Created by mturski on 11/8/2016.
 */
public final class XMLSlurperFactory {

    public static XMLSlurperFactory getInstance() {
        return new XMLSlurperFactory();
    }

    private boolean isNamespaceAwarenessDisabled;
    private boolean isDTDValidationDisabled;

    private XMLSlurperFactory() {
    }

    /**
     * Disables namespace awareness. By default namespace awareness is turned on. By disabling namespace awareness, the
     * parser created with {@link XMLSlurperFactory#createXMLSlurper} will ignore any namespace related data. For
     * convenience the following method returns <code>this</code> instance of {@link XMLSlurperFactory}.
     *
     * @return <code>this</code> instance of <code>XMLSlurperFactory</code>
     */
    public final XMLSlurperFactory disableNamespaceAwareness() {
        this.isNamespaceAwarenessDisabled = true;

        return this;
    }

    /**
     * Upon finding &lt;!DOCTYPE &gt; no schema will be searched for both locally and via web. By default, a
     * {@link DefaultHandler} will be used to resolve entities. For convenience the following method returns
     * <code>this</code> instance of {@link XMLSlurperFactory}.
     *
     * @return <code>this</code> instance of <code>XMLSlurperFactory</code>
     */
    public final XMLSlurperFactory disableDTDValidation() {
        this.isDTDValidationDisabled = true;

        return this;
    }

    /**
     * Returns a new instance of {@link XMLSlurper} with namespace awareness configured with
     * {@link XMLSlurperFactory#disableNamespaceAwareness}.
     *
     * @return a new instance of <code>XMLSlurper</code>
     */
    public final XMLSlurper createXMLSlurper() {
        XMLNodeFactory xmlNodeFactory = XMLNodeFactory.getInstance();
        NodeNotifier nodeNotifier = getNodeNotifier();
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();

        return new SAXSlurper(
                getSaxParserFactory(isNamespaceAwarenessDisabled),
                getSchemaFactory(),
                getSlurpFactory(nodeNotifier, slurpAlignmentFactory),
                nodeNotifier,
                getSAXNamespaceSensitiveElementParser(isNamespaceAwarenessDisabled, xmlNodeFactory),
                getEntityResolver(isDTDValidationDisabled));
    }

    static EntityResolver getEntityResolver(boolean isDTDValidationDisabled) {
        if (isDTDValidationDisabled)
            return new SkipDTDDownloadEntityResolver();
        else
            return new DefaultEntityResolver();
    }

    static NodeNotifier getNodeNotifier() {
        return new NodeNotifier(new PositionCounter());
    }

    static SlurpAlignmentFactory getSlurpAlignmentFactory() {
        return new SlurpAlignmentFactory();
    }

    static SlurpFactory getSlurpFactory(
            NodeNotifier nodeNotifier,
            SlurpAlignmentFactory slurpAlignmentFactory) {
        return new SlurpFactory(nodeNotifier, slurpAlignmentFactory);
    }

    static SAXParserFactory getSaxParserFactory(boolean isNamespaceAwarenessDisabled) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        try {
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setFeature("http://xml.org/sax/features/namespaces", true);

            if (isNamespaceAwarenessDisabled)
                saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            else
                saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException(e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException(e);
        }

        return saxParserFactory;
    }

    static SchemaFactory getSchemaFactory() {
        return SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    static ElementParser getSAXNamespaceSensitiveElementParser(
            boolean isNamespaceAwarenessDisabled, XMLNodeFactory xmlNodeFactory) {

        if (isNamespaceAwarenessDisabled)
            return new SAXNamespaceBlindElementParser(xmlNodeFactory);
        else
            return new SAXNamespaceAwareElementParser(xmlNodeFactory);
    }
}
