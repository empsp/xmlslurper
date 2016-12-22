package org.xs4j;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xs4j.NodeNotifier.FindData;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mturski on 11/8/2016.
 */
public class XMLSlurperFactory {
    public static XMLSlurperFactory getInstance() {
        return new XMLSlurperFactory();
    }

    private boolean isNamespaceAwarenessDisabled;

    private XMLSlurperFactory() {
    }

    /**
     * Disables namespace awareness. By default namespace awareness is turned on. By disabling namespace awareness, the
     * parser created with {@link XMLSlurperFactory#createXMLSlurper} will ignore any namespace related data. For
     * convenience the following method returns <code>this</code> instance of {@link XMLSlurperFactory}.
     *
     * @return <code>this</code> instance of <code>XMLSlurperFactory</code>
     */
    public XMLSlurperFactory disableNamespaceAwareness() {
        this.isNamespaceAwarenessDisabled = true;

        return this;
    }

    /**
     * Returns a new instance of {@link XMLSlurper} with namespace awareness configured with
     * {@link XMLSlurperFactory#disableNamespaceAwareness}.
     *
     * @return a new instance of <code>XMLSlurper</code>
     */
    public XMLSlurper createXMLSlurper() {
        List<FindData> findData = new ArrayList<FindData>();
        List<FindData> findAllData = new ArrayList<FindData>();

        XMLNodeFactory xmlNodeFactory = XMLNodeFactory.getInstance();
        SlurpAlignmentFactory slurpAlignmentFactory = getSlurpAlignmentFactory();

        return new SAXSlurper(
                getSaxParserFactory(isNamespaceAwarenessDisabled),
                getSchemaFactory(),
                getSlurpFactory(findData, findAllData, slurpAlignmentFactory),
                getNodeNotifier(findData, findAllData),
                getSAXNamespaceSensitiveElementParser(isNamespaceAwarenessDisabled, xmlNodeFactory));
    }

    static NodeNotifier getNodeNotifier(List<FindData> findData,
                                        List<FindData> findAllData) {
        return new NodeNotifier(new PositionCounter(), findData, findAllData);
    }

    static SlurpAlignmentFactory getSlurpAlignmentFactory() {
        return new SlurpAlignmentFactory();
    }

    static SlurpFactory getSlurpFactory(
            List<FindData> findData,
            List<FindData> findAllData,
            SlurpAlignmentFactory slurpAlignmentFactory) {
        return new SlurpFactory(findData, findAllData, slurpAlignmentFactory);
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

    static NamespaceSensitiveElementParser getSAXNamespaceSensitiveElementParser(boolean isNamespaceAwarenessDisabled, XMLNodeFactory xmlNodeFactory) {
        if (isNamespaceAwarenessDisabled)
            return new SAXSlurper.SAXNamespaceBlindElementParser(xmlNodeFactory);
        else
            return new SAXSlurper.SAXNamespaceAwareElementParser(xmlNodeFactory);
    }
}
