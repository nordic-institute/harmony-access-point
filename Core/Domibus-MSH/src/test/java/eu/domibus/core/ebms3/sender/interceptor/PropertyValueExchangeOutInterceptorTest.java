package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class PropertyValueExchangeOutInterceptorTest extends SoapInterceptorTest {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyValueExchangeOutInterceptorTest.class);

    @Tested
    PropertyValueExchangeOutInterceptor propertyValueExchangeOutInterceptor;

    @Test
    public void testHandleMessage() throws XMLStreamException, ParserConfigurationException, SOAPException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        SoapMessage soapMessage = getSoapMessageForDom(doc);
        propertyValueExchangeOutInterceptor.handleMessage(soapMessage);
        Assert.assertEquals(soapMessage.get(MSHDispatcher.MESSAGE_TYPE_OUT), MESSAGE_TYPE_OUT_TEST_VALUE);
    }
}
