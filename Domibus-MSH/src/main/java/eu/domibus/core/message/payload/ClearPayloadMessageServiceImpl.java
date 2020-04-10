package eu.domibus.core.message.payload;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Service
public class ClearPayloadMessageServiceImpl implements ClearPayloadMessageService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ClearPayloadMessageServiceImpl.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    @Qualifier("clearPayloadQueue")
    private Queue clearPayloadQueue;

    @Autowired
    protected JMSManager jmsManager;

    @Override
    public void clearPayloadData(final String messageId) {
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            LOG.debug("Message [{}] does not exist", messageId);
            return;
        }
        //clear payload data
        messagingDao.clearPayloadData(userMessage);
    }

    @Override
    public void enqueueMessageForClearPayload(UserMessage userMessage) {
        //check before enqueuing the message
        if (userMessage.getPayloadInfo() == null || CollectionUtils.isEmpty(userMessage.getPayloadInfo().getPartInfo())) {
            LOG.debug("No payloads to clear");

            //no JMS message will be enqueued if there is no payload to clear
            return;
        }

        String messageId = userMessage.getMessageInfo().getMessageId();

        final JmsMessage message = createJMSMessage(messageId);
        jmsManager.sendMessageToQueue(message, clearPayloadQueue);
    }

    private JmsMessage createJMSMessage(String messageId) {
        return JMSMessageBuilder.create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .build();
    }

}
