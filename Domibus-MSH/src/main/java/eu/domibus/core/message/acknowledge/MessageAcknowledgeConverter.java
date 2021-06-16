package eu.domibus.core.message.acknowledge;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.model.UserMessage;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeConverter {

    MessageAcknowledgementEntity create(String user, UserMessage userMessage, Timestamp acknowledgeTimestamp, String from, String to);

    MessageAcknowledgement convert(MessageAcknowledgementEntity entity);

    List<MessageAcknowledgement> convert(List<MessageAcknowledgementEntity> entities);
}
