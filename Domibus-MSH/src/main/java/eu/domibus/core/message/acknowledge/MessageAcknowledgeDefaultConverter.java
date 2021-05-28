package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.model.UserMessage;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class MessageAcknowledgeDefaultConverter implements MessageAcknowledgeConverter {

    @Override
    public MessageAcknowledgementEntity create(String user, UserMessage userMessage, Timestamp acknowledgeTimestamp, String from, String to) {
        MessageAcknowledgementEntity result = new MessageAcknowledgementEntity();
        result.setUserMessage(userMessage);
        result.setAcknowledgeDate(acknowledgeTimestamp);
        result.setCreationTime(new Timestamp(System.currentTimeMillis()));
        result.setCreatedBy(user);
        result.setFrom(from);
        result.setTo(to);
        return result;
    }


    @Override
    public MessageAcknowledgement convert(MessageAcknowledgementEntity entity) {
        MessageAcknowledgement result = new MessageAcknowledgement();
        result.setId(entity.getEntityId());
        result.setMessageId(entity.getUserMessage().getMessageId());
        result.setFrom(entity.getFrom());
        result.setTo(entity.getTo());
        result.setCreateDate(new Timestamp(entity.getCreationTime().getTime()));
        result.setCreateUser(entity.getCreatedBy());
        result.setAcknowledgeDate(entity.getAcknowledgeDate());
        return result;
    }

    @Override
    public List<MessageAcknowledgement> convert(List<MessageAcknowledgementEntity> entities) {
        if (entities == null) {
            return null;
        }
        List<MessageAcknowledgement> result = new ArrayList<>();
        for (MessageAcknowledgementEntity entity : entities) {
            result.add(convert(entity));
        }
        return result;
    }
}
