package eu.domibus.core.message.retention;

import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLogDto;

import java.util.List;

/**
 * Responsible for the retention and clean up of Domibus messages, including signal messages. *
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageRetentionService {

    /**
     * Deletes the expired messages(downloaded or not) using the configured limits
     */
    void deleteExpiredMessages();

    void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit, Integer expiredSentMessagesLimit, Integer expiredPayloadDeletedMessagesLimit);

    void scheduleDeleteMessages(List<String> messageIds);

    void scheduleDeleteMessagesByMessageLog(List<UserMessageLogDto> userMessageLogs);

    void scheduleDeleteMessagesByMessageLog(List<UserMessageLogDto> userMessageLogs, int maxBatch);

    void deletePayloadOnSendSuccess(UserMessage userMessage, UserMessageLog userMessageLog);

    void deletePayloadOnSendFailure(UserMessage userMessage, UserMessageLog userMessageLog);
}
