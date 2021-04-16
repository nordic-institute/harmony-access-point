package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3UserMessage;
import eu.domibus.api.model.UserMessage;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
public interface Ebms3UserMessageMapper {

    Ebms3UserMessage userMessageEntityToEbms3(UserMessage userMessage);

    UserMessage userMessageEbms3ToEntity(Ebms3UserMessage ebms3UserMessage);
}
