package eu.domibus.api.usermessage;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;

public interface UserMessageLogService {

    MessageStatus getMessageStatus(final Long messageEntityId);

    MessageStatus getMessageStatus(String messageId, MSHRole mshRole);

    MessageStatus getMessageStatusById(String messageId);

    UserMessageLog findByMessageId(String messageId);

    UserMessageLog findByMessageId(String messageId, MSHRole mshRole);
}
