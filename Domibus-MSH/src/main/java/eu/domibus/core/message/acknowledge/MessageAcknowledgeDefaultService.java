package eu.domibus.core.message.acknowledge;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.acknowledge.MessageAcknowledgeException;
import eu.domibus.api.message.acknowledge.MessageAcknowledgeService;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageAcknowledgeDefaultService implements MessageAcknowledgeService {

    @Autowired
    MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    MessageAcknowledgementPropertyDao messageAcknowledgementPropertyDao;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    MessageAcknowledgeConverter messageAcknowledgeConverter;

    @Autowired
    UserMessageServiceHelper userMessageServiceHelper;


    @Transactional
    @Override
    public MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws MessageAcknowledgeException {
        final UserMessage userMessage = getUserMessage(messageId);
        final String localAccessPointId = getLocalAccessPointId(userMessage);
        final String finalRecipient = userMessageServiceHelper.getFinalRecipient(userMessage);
        return acknowledgeMessage(userMessage, acknowledgeTimestamp, localAccessPointId, finalRecipient, properties);
    }

    protected UserMessage getUserMessage(String messageId) {
        final UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageAcknowledgeException(DomibusCoreErrorCode.DOM_001, "Message with ID [" + messageId + "] does not exist");
        }
        return userMessage;
    }

    @Transactional
    @Override
    public MessageAcknowledgement acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws MessageAcknowledgeException {
        return acknowledgeMessageDelivered(messageId, acknowledgeTimestamp, null);
    }

    @Transactional
    @Override
    public MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws MessageAcknowledgeException {
        final UserMessage userMessage = getUserMessage(messageId);
        final String localAccessPointId = getLocalAccessPointId(userMessage);
        final String finalRecipient = userMessageServiceHelper.getFinalRecipient(userMessage);
        return acknowledgeMessage(userMessage, acknowledgeTimestamp, finalRecipient, localAccessPointId, properties);
    }

    @Transactional
    @Override
    public MessageAcknowledgement acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws MessageAcknowledgeException {
        return acknowledgeMessageProcessed(messageId, acknowledgeTimestamp, null);
    }


    protected MessageAcknowledgement acknowledgeMessage(final UserMessage userMessage, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException {
        final String user = authUtils.getAuthenticatedUser();
        MessageAcknowledgementEntity entity = messageAcknowledgeConverter.create(user, userMessage, acknowledgeTimestamp, from, to);
        messageAcknowledgementDao.create(entity);

        if (properties != null) {
            properties.entrySet().forEach(entry -> {
                String name = entry.getKey();
                String value = entry.getValue();
                MessageAcknowledgementProperty property = new MessageAcknowledgementProperty();
                property.setName(name);
                property.setValue(value);
                property.setAcknowledgementEntity(entity);
                messageAcknowledgementPropertyDao.create(property);
            });
        }

        return messageAcknowledgeConverter.convert(entity);
    }

    @Override
    public List<MessageAcknowledgement> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException {
        final List<MessageAcknowledgementEntity> entities = messageAcknowledgementDao.findByMessageId(messageId);
        if (CollectionUtils.isEmpty(entities) && userMessageDao.findByMessageId(messageId) == null) {
            throw new MessageNotFoundException(messageId);
        }
        return messageAcknowledgeConverter.convert(entities);
    }

    protected String getLocalAccessPointId(UserMessage userMessage) {
        return userMessageServiceHelper.getPartyTo(userMessage);
    }

}
