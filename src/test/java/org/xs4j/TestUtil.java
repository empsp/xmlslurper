package org.xs4j;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Created by mturski on 12/12/2016.
 */
public class TestUtil {
    private static final XMLNodeFactory xmlNodeFactory = XMLNodeFactory.getInstance();

    public static <T> InputStream getResource(T testSuite, String resourceName) {
        return testSuite.getClass().getResourceAsStream(resourceName);
    }

    public static <T> File getResourceAsFile(T testSuite, String resourceName) {
        return new File(testSuite.getClass().getResource(resourceName).getPath());
    }

    public static XMLNode createNode(long id, String localName) {
        return xmlNodeFactory.createNode(id, localName, Collections.<String, String> emptyMap());
    }

    public static XMLNode createNode(long id, String namespace, String prefix, String localName, Map<String, String> attributeByQName) {
        return xmlNodeFactory.createNode(id, namespace, prefix, localName, attributeByQName);
    }
}
