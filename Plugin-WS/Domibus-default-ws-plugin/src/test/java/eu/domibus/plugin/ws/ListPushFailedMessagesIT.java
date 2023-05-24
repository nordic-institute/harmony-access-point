package eu.domibus.plugin.ws;

import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.MessagingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.generated.ListPushFailedMessagesFault;
import eu.domibus.plugin.ws.generated.body.ListPushFailedMessagesRequest;
import eu.domibus.plugin.ws.generated.body.ListPushFailedMessagesResponse;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
@Transactional
public class ListPushFailedMessagesIT extends AbstractBackendWSIT {

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
    public void listPushFailedMessagesWithEmptyMessageId() {
        String emptyMessageId = "  ";

        ListPushFailedMessagesRequest listPushFailedMessagesRequest = createListPushFailedMessagesRequest(emptyMessageId, null);
        try {
            webServicePluginInterface.listPushFailedMessages(listPushFailedMessagesRequest);
            Assert.fail();
        } catch (ListPushFailedMessagesFault listPushFailedMessagesFault) {
            String message = "Message ID is empty";
            Assert.assertEquals(message, listPushFailedMessagesFault.getMessage());
        }
    }

    @Test
    public void istPushFailedMessages_MessageId_Too_Long() {
        String invalidMessageId = StringUtils.repeat("X", 256);
        ListPushFailedMessagesRequest listPushFailedMessagesRequest = createListPushFailedMessagesRequest(invalidMessageId, null);
        try {
            webServicePluginInterface.listPushFailedMessages(listPushFailedMessagesRequest);
            Assert.fail();
        } catch (ListPushFailedMessagesFault listPushFailedMessagesFault) {
            String message = "Invalid Message Id. ";
            Assert.assertEquals(message, listPushFailedMessagesFault.getMessage());
        }
    }

    @Test
    public void istPushFailedMessages_originalSender() throws ListPushFailedMessagesFault {
        String messageId = "messageId";
        String originalSender = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1-5 ";

        ListPushFailedMessagesRequest listPushFailedMessagesRequest = createListPushFailedMessagesRequest(messageId, originalSender);

        ListPushFailedMessagesResponse response = webServicePluginInterface.listPushFailedMessages(listPushFailedMessagesRequest);
        Assert.assertEquals(response.getMessageID(), Collections.emptyList());
    }


    private ListPushFailedMessagesRequest createListPushFailedMessagesRequest(String messageId, String originalSender) {
        ListPushFailedMessagesRequest listPushFailedMessagesRequest = new ListPushFailedMessagesRequest();
        listPushFailedMessagesRequest.setMessageId(messageId);
        listPushFailedMessagesRequest.setOriginalSender(originalSender);
        return listPushFailedMessagesRequest;
    }
}
