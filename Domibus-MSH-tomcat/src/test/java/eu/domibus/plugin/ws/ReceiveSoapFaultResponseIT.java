package eu.domibus.plugin.ws;


import eu.domibus.AbstractBackendWSIT;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.common.LoggerUtil;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.io.IOException;

/**
 * @author idragusa
 * @since 5.0
 */
@DirtiesContext
@Rollback
public class ReceiveSoapFaultResponseIT extends AbstractBackendWSIT {

    @Autowired
    LoggerUtil loggerUtil;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
        loggerUtil.updateLogger();
    }

    @After
    public void cleanupLogger() {
        loggerUtil.cleanupLogger();
    }

    @Test
    public void testReceiveValidSoapFault() throws SubmitMessageFault {
        submitMessage(MessageStatus.WAITING_FOR_RETRY, "SOAPFaultResponse.xml");

        Assert.assertTrue(loggerUtil.verifyLogging("SOAPFaultException: Policy Falsified"));
    }
}
