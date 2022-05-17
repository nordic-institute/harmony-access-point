package eu.domibus.api.usermessage;

import java.util.List;

/**
 * @author Soumya
 * @since 4.2.2
 */
public interface UserMessageRestoreService {

    void restoreFailedMessage(String messageId);

    /**
     * Resend a message in the status SEND_FAILURE or
     * SEND_ENQUEUED
     *
     * @param messageId message Id of the message
     */
    void resendFailedOrSendEnqueuedMessage(final String messageId);

    List<String> restoreFailedMessagesDuringPeriod(Long begin, Long end, String finalRecipient, String originalUser);

}