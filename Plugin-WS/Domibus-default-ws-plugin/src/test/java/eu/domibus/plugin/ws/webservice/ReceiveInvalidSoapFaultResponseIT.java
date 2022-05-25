package eu.domibus.plugin.ws.webservice;


import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.LoggerUtil;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author idragusa
 * @since 5.0
 */
public class ReceiveInvalidSoapFaultResponseIT extends AbstractBackendWSIT {

    @Autowired
    LoggerUtil loggerUtil;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
        loggerUtil.addByteArrayOutputStreamAppender();
    }

    @After
    public void cleanupLogger() {
        loggerUtil.cleanupByteArrayOutputStreamAppender();
    }

    @Test
    @Ignore("[EDELIVERY-8828] WSPLUGIN: tests for rest methods ignored")
    public void testReceiveValidSoapFault() throws SubmitMessageFault {
//        submitMessage(MessageStatus.WAITING_FOR_RETRY, "InvalidSOAPFaultResponse.xml");
        Assert.assertTrue(loggerUtil.verifyLogging("SOAPFaultException: Invalid SOAP fault content"));
    }
}