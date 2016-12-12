package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xs4j.listener.NodeListener;

import java.io.OutputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.xs4j.TestUtil.getResource;

/**
 * Created by mturski on 12/12/2016.
 */
public class XMLSpitterIT {
    @Test
    public void givenInputXMLExtractAPartIntoNewXML() throws Exception {
        // given
        final OutputStream outputStream = mock(OutputStream.class);

        final XMLSpitter xmlSpitter = XMLSpitterFactory.getInstance().createXMLSpitter();
        final XMLStream[] streams = new XMLStream[1];

        // when
        XMLSlurper xmlSlurper = XMLSlurperFactory.getInstance().createXMLSlurper();
        xmlSlurper.getNodes("**", "Plane").find(new NodeListener() {
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
        xmlSlurper.getNodes("**", "Plane", "**").findAll(new NodeListener() {
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
        xmlSlurper.parse(getResource(this, "siblingsTestCase.xml"));

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

    private String getXMLData(List<Integer> actualData) {
        StringBuilder sb = new StringBuilder();
        for (Integer character : actualData)
            sb.append((char)character.byteValue());

        return sb.toString();
    }
}
