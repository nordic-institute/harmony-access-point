package eu.domibus.core.message.retention;

/**
 * Responsible for the retention and clean up of Domibus messages, including signal messages. *
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageRetentionService {

    /**
     * Deletes the expired messages using the configured limits
     */
    void deleteExpiredMessages();

    /**
     * If the class handles the deletion strategy
     * @param retentionStrategy
     * @return
     */
    boolean handlesDeletionStrategy(String retentionStrategy);

//    void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit, Integer expiredSentMessagesLimit, Integer expiredPayloadDeletedMessagesLimit);
//
//    void scheduleDeleteMessages(List<String> messageIds);
//
//    void scheduleDeleteMessagesByMessageLog(List<UserMessageLogDto> userMessageLogs);
//
//    void deleteMessages(List<UserMessageLogDto> userMessageLogs, int maxBatch);
//
//    void deletePayloadOnSendSuccess(UserMessage userMessage, UserMessageLog userMessageLog);
//
//    void deletePayloadOnSendFailure(UserMessage userMessage, UserMessageLog userMessageLog);
}
