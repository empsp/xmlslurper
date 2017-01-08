package org.xs4j;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

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
 * Created by mturski on 1/7/2017.
 */
@SuppressWarnings("Duplicates")
public class InternalSpitterIT {
    private static final XMLSpitterFactory xmlSpitterFactory = InternalSpitterFactory.getInstance();
    private static final OutputSupplierFactory outputSupplierFactory = OutputSupplierFactory.getInstance();
    private static final XMLSpitter xmlSpitter = xmlSpitterFactory.createXMLSpitter();
    private static final XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
    private static final SpitterUtil spitterUtil = new SpitterUtil(xmlSlurper, xmlSpitter);

    @Test
    public void givenInputXMLWithoutSchemaExtractAPartIntoNewXMLWithIgnorableWhitespaceCharacters() throws Exception {
        // given
        OutputStream outputStream = spy(new OutputStreamStub());

        // when
        spitterUtil.createXML(outputStream, asList("**", "Plane"), asList("**", "Plane", "**"), "siblingsTestCase.xml");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verify(outputStream).close();
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
        OutputStream outputStream = spy(new OutputStreamStub());

        // when
        spitterUtil.createXML(outputStream, asList("ObjectTree"), asList("ObjectTree", "Object"), "borderTestCase.xml", "borderTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ObjectTree><Object attr2=\"E=1\" attr1=\"SOMEOBJ\"></Object></ObjectTree>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractAPartIntoNewXMLWithSignificantWhitespaceCharacters() throws Exception {
        // given
        OutputStream outputStream = spy(new OutputStreamStub());

        // when
        spitterUtil.createXML(outputStream, asList("ObjectTree"), asList("ObjectTree", "OtherObject"), "borderTestCase.xml", "borderTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ObjectTree><OtherObject attr3=\"123\">\n        \n        \n    </OtherObject></ObjectTree>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractAPartIntoNewXMLWithForeignNamespaceDefinitionIncluded() throws Exception {
        // given
        OutputStream outputStream = spy(new OutputStreamStub());

        // when
        spitterUtil.createXML(outputStream, asList("**", "other:OtherObject"), asList("**", "other:OtherObject", "**"), "namespaceTestCase.xml", "namespaceTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><other:OtherObject xmlns:other=\"http://other\" other:attr3=\"123\"><OtherObject xmlns=\"http://general\" attr2=\"OTHER =123,OTHER_OBJECT= 124\"></OtherObject><OtherObject xmlns=\"http://general\" xmlns:other=\"http://other\" other:attr2=\"OTHER = 123,OTHER_OBJECT = 125\"></OtherObject></other:OtherObject>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractAPartIntoNewXMLWithDefaultNamespaceDefinitionIncluded() throws Exception {
        // given
        OutputStream outputStream = spy(new OutputStreamStub());

        // when
        spitterUtil.createXML(outputStream, asList("**", "Object"), asList("**", "Object", "**"), "namespaceTestCase.xml", "namespaceTestCaseSchema.xsd");

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Object xmlns=\"http://general\" attr2=\"E=1\" attr1=\"SOMEOBJ\"><OtherObject attr1=\"OTHEROBJ\"></OtherObject></Object>";
        assertThat(actualXML, is(expectedXML));
    }

    @Test
    public void givenInputXMLWithSchemaExtractWithFormattingIntoNewXML() throws Exception {
        // given
        OutputStream outputStream = spy(new OutputStreamStub());
        OutputSupplier<OutputStream> osSupplier = outputSupplierFactory.<OutputStream> createOutputSupplier().set(outputStream);

        // when
        xmlSpitter.writeAll(xmlSlurper.getNodes("**", "Object"), xmlSlurper.getNodes("**", "Object", "**"), osSupplier, "1.0", "UTF-8");
        xmlSlurper.parse(getResource(this, "namespaceTestCase.xml"), getResourceAsFile(this, "namespaceTestCaseSchema.xsd"));

        // then
        ArgumentCaptor<Integer> streamCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(outputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream, atLeastOnce()).write(streamCaptor.capture());
        verify(outputStream).flush();
        verify(outputStream).close();
        verifyNoMoreInteractions(outputStream);

        String actualXML = getXMLData(streamCaptor.getAllValues());
        String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Object xmlns=\"http://general\" attr2=\"E=1\" attr1=\"SOMEOBJ\">\n    <OtherObject attr1=\"OTHEROBJ\">\n    </OtherObject>\n</Object>";
        assertThat(actualXML, is(expectedXML));
    }

    private class OutputStreamStub extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }

    private static String getXMLData(List<Integer> actualData) {
        String result = "";

        for (Integer character : actualData)
            result += (char)character.byteValue();

        return result;
    }
}
