package eu.domibus.api.message;

import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;

/**
 * Interface for the Service class of SignalMessageLog
 * @author Tiago Miguel
 * @since 4.0
 */
public interface SignalMessageLogService {

    void save(SignalMessage signalMessage, String userMessageService, String userMessageAction);
}
