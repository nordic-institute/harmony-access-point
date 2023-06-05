package eu.domibus.api.ebms3.adapter;

import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.core.util.xml.XMLUtilImpl;
import mockit.Expectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@RunWith(JMockit.class)
public class Ebms3ToStringAdapterTest {

    @Tested
    ToStringAdapter toStringAdapter;

    @Test
    public void testToStringToNode() throws IOException, TransformerException {
        new Expectations(SpringContextProvider.class) {{
            SpringContextProvider.getApplicationContext().getBean(XMLUtil.BEAN_NAME, XMLUtil.class);
            result = new XMLUtilImpl(null);
        }};

        final String receiptPath = "dataset/as4/MSHAS4Response.xml";
        String receipt = IOUtils.toString(new ClassPathResource(receiptPath).getInputStream(), StandardCharsets.UTF_8);

        Node node = toStringAdapter.stringToNode(receipt);
        Node resultNode = toStringAdapter.stringToNode(toStringAdapter.nodeToString(node));
        assertEquals(node.getTextContent(), resultNode.getTextContent());
    }
}
