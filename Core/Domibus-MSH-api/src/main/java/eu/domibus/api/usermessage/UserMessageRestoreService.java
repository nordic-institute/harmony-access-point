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

    /**
     * @param failedStartDate date and time (in ID PK format) from which to restore failed messages
     * @param failedEndDate date and time (in ID PK format) until which to restore failed messages
     * @param finalRecipient final recipient of failed message
     * @param originalUser original user of failed message
     * @return the failed messages in the given period and with the given final recipient and original user
     * that were successfully restored
     */
    List<String> restoreFailedMessagesDuringPeriod(Long failedStartDate, Long failedEndDate, String finalRecipient, String originalUser);

}