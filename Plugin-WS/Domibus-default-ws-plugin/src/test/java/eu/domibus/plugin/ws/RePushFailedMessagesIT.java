package eu.domibus.plugin.ws;


import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.MessagingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.generated.RePushFailedMessagesFault;
import eu.domibus.plugin.ws.generated.body.RePushFailedMessagesRequest;
import eu.domibus.test.DomibusConditionUtil;
import eu.domibus.test.PModeUtil;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Transactional
public class RePushFailedMessagesIT extends AbstractBackendWSIT {

    @Autowired
    MessagingService messagingService;

    @Autowired
    MSHWebservice mshWebserviceTest;

    @Autowired
    DomibusConditionUtil domibusConditionUtil;

    @Autowired
    PModeUtil pModeUtil;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Before
    public void before() throws IOException, XmlProcessingException {
        domibusConditionUtil.waitUntilDatabaseIsInitialized();
        pModeUtil.uploadPmode(wireMockRule.port());
    }

    @Test
    public void rePushFailedMessagesInvalidMessageId() {
        List<String> messageIds = new ArrayList<>();
        String invalidMessageId = "invalid";
        messageIds.add(invalidMessageId);
        RePushFailedMessagesRequest rePushRequest = createRePushFailedMessagesRequest(messageIds);
        try {
            webServicePluginInterface.rePushFailedMessages(rePushRequest);
            Assert.fail();
        } catch (RePushFailedMessagesFault rePushFault) {
            String message = "Invalid Message Id. ";
            Assert.assertEquals(message, rePushFault.getMessage());
        }
    }

    @Test
    public void rePushFailedMessagesEmptyMessageIds() {
        List<String> messageIds = new ArrayList<>();
        String emptyMessageId = " ";
        messageIds.add(emptyMessageId);
        RePushFailedMessagesRequest rePushRequest = createRePushFailedMessagesRequest(messageIds);
        try {
            webServicePluginInterface.rePushFailedMessages(rePushRequest);
            Assert.fail();
        } catch (RePushFailedMessagesFault rePushFault) {
            String message = "Message ID is empty";
            Assert.assertEquals(message, rePushFault.getMessage());
        }
    }

    @Test
    public void rePushFailedMessagesMessageId_Too_Long() {
        List<String> messageIds = new ArrayList<>();
        String invalidMessageId = StringUtils.repeat("X", 256);
        messageIds.add(invalidMessageId);
        RePushFailedMessagesRequest rePushRequest = createRePushFailedMessagesRequest(messageIds);
        try {
            webServicePluginInterface.rePushFailedMessages(rePushRequest);
            Assert.fail();
        } catch (RePushFailedMessagesFault rePushFault) {
            String message = "Invalid Message Id. ";
            Assert.assertEquals(message, rePushFault.getMessage());
        }
    }

    private RePushFailedMessagesRequest createRePushFailedMessagesRequest(final List<String> messageIds) {
        RePushFailedMessagesRequest rePushRequest = new RePushFailedMessagesRequest();
        rePushRequest.getMessageID().addAll(messageIds);
        return rePushRequest;
    }
}
