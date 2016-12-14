package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xml.sax.SAXException;
import org.xs4j.XMLSpitter.OutputStreamSupplier;
import org.xs4j.listener.NodeListener;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.xs4j.TestUtil.getResource;
import static org.xs4j.TestUtil.getResourceAsFile;

/**
 * Created by mturski on 12/12/2016.
 */
public class XMLSpitterIT {
    private static final XMLSpitterFactory xmlSpitterFactory = XMLSpitterFactory.getInstance();
    private static final XMLSpitter xmlSpitter = xmlSpitterFactory.createXMLSpitter();
    private static final XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();

    @Test
    public void givenInputXMLWithoutSchemaExtractAPartIntoNewXMLWithIgnorableWhitespaceCharacters() throws Exception {
        // given
        OutputStream outputStream = mock(OutputStream.class);

        // when
        createXML(outputStream, asList("**", "Plane"), asList("**", "Plane", "**"), "siblingsTestCase.xml");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Plane name=\"Boeing 747\" manufacturer=\"Boeing\"><Engine type=\"Turbofan\"></Engine><Cargo><Capacity>110m3</Capacity>\n" +
                "            \n" +
                "        </Cargo></Plane>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractAPartIntoNewXMLWithoutIgnorableWhitespaceCharacters() throws Exception {
        // given
        final OutputStream outputStream = mock(OutputStream.class);

        // when
        createXML(outputStream, asList("ObjectTree"), asList("ObjectTree", "Object"), "borderTestCase.xml", "borderTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ObjectTree><Object attr2=\"E=1\" attr1=\"SOMEOBJ\"></Object></ObjectTree>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractAPartIntoNewXMLWithSignificantWhitespaceCharacters() throws Exception {
        // given
        final OutputStream outputStream = mock(OutputStream.class);

        // when
        createXML(outputStream, asList("ObjectTree"), asList("ObjectTree", "OtherObject"), "borderTestCase.xml", "borderTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ObjectTree><OtherObject attr3=\"123\">\n        \n        \n    </OtherObject></ObjectTree>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractAPartIntoNewXMLWithForeignNamespaceDefinitionIncluded() throws Exception {
        // given
        final OutputStream outputStream = mock(OutputStream.class);

        // when
        createXML(outputStream, asList("**", "other:OtherObject"), asList("**", "other:OtherObject", "**"), "namespaceTestCase.xml", "namespaceTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><other:OtherObject other:attr3=\"123\"><OtherObject xmlns=\"http://general\" attr2=\"OTHER =123,OTHER_OBJECT= 124\"></OtherObject><OtherObject other:attr2=\"OTHER = 123,OTHER_OBJECT = 125\"></OtherObject></other:OtherObject>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractAPartIntoNewXMLWithDefaultNamespaceDefinitionIncluded() throws Exception {
        // given
        final OutputStream outputStream = mock(OutputStream.class);

        // when
        createXML(outputStream, asList("**", "Object"), asList("**", "Object", "**"), "namespaceTestCase.xml", "namespaceTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Object xmlns=\"http://general\" attr2=\"E=1\" attr1=\"SOMEOBJ\"><OtherObject attr1=\"OTHEROBJ\"></OtherObject></Object>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractWithFormattingIntoNewXML() throws Exception {
        // given
        final OutputStream outputStream = mock(OutputStream.class);
        OutputStreamSupplier osSupplier = xmlSpitterFactory.createOutputStreamSupplier().set(outputStream);

        // when
        xmlSpitter.splitAll(xmlSlurper.getNodes("**", "Object"), xmlSlurper.getNodes("**", "Object", "**"), osSupplier, "1.0", "UTF-8");
        xmlSlurper.parse(getResource(this, "namespaceTestCase.xml"), getResourceAsFile(this, "namespaceTestCaseSchema.xsd"));

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream, times(2)).flush();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Object xmlns=\"http://general\" attr2=\"E=1\" attr1=\"SOMEOBJ\">\n    <OtherObject attr1=\"OTHEROBJ\">\n    </OtherObject>\n</Object>";
        assertThat(actualXML, is(expectedXML));
    }

    private void createXML(
            final OutputStream outputStream,
            List<String> parentPath,
            List<String> descendantsPath,
            String resourceName) throws ParserConfigurationException, XMLStreamException, SAXException, IOException {

        configureParser(outputStream, parentPath, descendantsPath);

        xmlSlurper.parse(getResource(this, resourceName));
    }

    private void createXML(
            final OutputStream outputStream,
            List<String> parentPath,
            List<String> descendantsPath,
            String resourceName,
            String schemaName) throws ParserConfigurationException, XMLStreamException, SAXException, IOException {

        configureParser(outputStream, parentPath, descendantsPath);

        xmlSlurper.parse(getResource(this, resourceName), getResourceAsFile(this, schemaName));
    }

    private void configureParser(final OutputStream outputStream, List<String> parentPath, List<String> descendantsPath) {
        final XMLStream[] streams = new XMLStream[1];

        xmlSlurper.getNodes(parentPath.toArray(new String[0])).find(new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                streams[0] = xmlSpitter.write(outputStream);
                streams[0].writeStartElement(node);
            }
        }, new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                streams[0].writeEndElement();
                streams[0].close();
            }
        });
        xmlSlurper.getNodes(descendantsPath.toArray(new String[0])).findAll(new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                streams[0].writeStartElement(node);
            }
        }, new NodeListener() {
            @Override
            public void onNode(@Nullable XMLNode parent, @NotNull XMLNode node) {
                streams[0].writeCharacters(node.getText());
                streams[0].writeEndElement();
            }
        });
    }

    private static String getXMLData(List<Integer> actualData) {
        StringBuilder sb = new StringBuilder();
        for (Integer character : actualData)
            sb.append((char)character.byteValue());

        return sb.toString();
    }
}
