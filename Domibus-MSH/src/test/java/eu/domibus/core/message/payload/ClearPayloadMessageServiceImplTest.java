package eu.domibus.core.message.payload;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.PayloadInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.Collections;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ClearPayloadMessageServiceImplTest {

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    Queue clearPayloadQueue;

    @Injectable
    JMSManager jmsManager;

    @Tested
    ClearPayloadMessageServiceImpl clearPayloadMessageService;


    @Test
    public void testClearPayloadData_UserMessageFound(final @Mocked UserMessage userMessage) {
        final String messageId = "messageId-123456";

        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;
        }};

        //tested method
        clearPayloadMessageService.clearPayloadData(messageId);

        new FullVerifications() {{
            messagingDao.clearPayloadData(userMessage);
        }};
    }

    @Test
    public void testClearPayloadData_UserMessageNotFound() {
        final String messageId = "messageId-123456";

        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = null;
        }};

        //tested method
        clearPayloadMessageService.clearPayloadData(messageId);

        new FullVerifications() {{
        }};
    }

    @Test
    public void testEnqueueMessageForClearPayload_WithAttachments(final @Mocked UserMessage userMessage,
                                                                  final @Mocked PayloadInfo payloadInfo,
                                                                  final @Mocked JmsMessage jmsMessage) {
        final String messageId = "messageId-123456";
        final PartInfo partInfo = new PartInfo();
        final List<PartInfo> list = Collections.singletonList(partInfo);
        new Expectations(clearPayloadMessageService) {{
            userMessage.getPayloadInfo();
            result = payloadInfo;

            payloadInfo.getPartInfo();
            result = list;

            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            clearPayloadMessageService.createJMSMessage(messageId);
            result = jmsMessage;
        }};

        //tested method
        clearPayloadMessageService.enqueueMessageForClearPayload(userMessage);

        new FullVerifications(clearPayloadMessageService) {{
            jmsManager.sendMessageToQueue(jmsMessage, clearPayloadQueue);
        }};
    }

    @Test
    public void testEnqueueMessageForClearPayload_NoAttachments(final @Mocked UserMessage userMessage) {
        new Expectations() {{
            userMessage.getPayloadInfo();
            result = null;
        }};

        //tested method
        clearPayloadMessageService.enqueueMessageForClearPayload(userMessage);

        new FullVerifications() {{
        }};
    }
}