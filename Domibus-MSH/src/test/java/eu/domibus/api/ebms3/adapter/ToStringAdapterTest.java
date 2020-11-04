package eu.domibus.api.ebms3.adapter;

import mockit.Tested;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author idragusa
 * @since 4.1
 */
public class ToStringAdapterTest {

    @Tested
    ToStringAdapter toStringAdapter;

    @Test
    public void testToStringToNode() throws IOException, TransformerException {

        final String receiptPath = "dataset/as4/MSHAS4Response.xml";
        String receipt = IOUtils.toString(new ClassPathResource(receiptPath).getInputStream(), StandardCharsets.UTF_8);

        Node node = toStringAdapter.stringToNode(receipt);
        Node resultNode = toStringAdapter.stringToNode(toStringAdapter.nodeToString(node));
        assertEquals(node.getTextContent(), resultNode.getTextContent());
    }
}
