package eu.domibus.api.usermessage;

import eu.domibus.api.model.MessageStatus;

public interface UserMessageLogService {

    MessageStatus getMessageStatus(final Long messageEntityId);
}
