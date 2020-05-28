
package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.message.pull.MessagingLockDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Ioana Dragusanu,
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class RetryDefaultServiceTest {
    private static List<String> QUEUED_MESSAGEIDS = Arrays.asList("queued123@domibus.eu", "queued456@domibus.eu", "queued789@domibus.eu");

    @Tested
    private RetryDefaultService retryService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private Queue sendMessageQueue;

    @Injectable
    private Queue sendLargeMessageQueue;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    UpdateRetryLoggingService updateRetryLoggingService;

    private List<JmsMessage> getQueuedMessages() {
        List<JmsMessage> jmsMessages = new ArrayList<>();
        for (String messageId : QUEUED_MESSAGEIDS) {
            JmsMessage jmsMessage = new JmsMessage();
            jmsMessage.setProperty(MessageConstants.MESSAGE_ID, messageId);
            jmsMessages.add(jmsMessage);
        }
        return jmsMessages;
    }

    @Test
    public void getMessagesNotAlreadyQueuedWithNoAlreadyQueuedMessagesTest() {
        List<String> retryMessageIds = Arrays.asList("retry123@domibus.eu", "retry456@domibus.eu", "expired123@domibus.eu");

        new NonStrictExpectations(retryService) {{
            userMessageLogDao.findRetryMessages();
            result = new ArrayList<>(retryMessageIds);
        }};

        List<String> result = retryService.getMessagesNotAlreadyScheduled();
        assertEquals(3, result.size());

        assertEquals(result, retryMessageIds);
    }
}