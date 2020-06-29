package eu.domibus.api.usermessage;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.usermessage.domain.UserMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageService {

    String COMMAND_SOURCE_MESSAGE_REJOIN = "SourceMessageRejoin";
    String COMMAND_SOURCE_MESSAGE_RECEIPT = "SourceMessageReceipt";
    String COMMAND_SOURCE_MESSAGE_REJOIN_FILE = "SourceMessageRejoinFile";
    String COMMAND_SPLIT_AND_JOIN_SEND_FAILED = "SplitAndJoinSendFailed";
    String COMMAND_SET_MESSAGE_FRAGMENT_AS_FAILED = "SetMessageFragmentAsFailed";
    String COMMAND_SEND_SIGNAL_ERROR = "SendSignalError";
    String COMMAND_SPLIT_AND_JOIN_RECEIVE_FAILED = "SplitAndJoinReceiveFailed";


    String MSG_SOURCE_MESSAGE_FILE = "SourceMessageFile";
    String MSG_TYPE = "messageType";
    String MSG_GROUP_ID = "groupId";
    String MSG_BACKEND_NAME = "backendName";
    String MSG_SOURCE_MESSAGE_ID = "sourceMessageId";
    String MSG_USER_MESSAGE_ID = "userMessageId";
    String MSG_EBMS3_ERROR_CODE = "ebms3ErrorCode";
    String MSG_EBMS3_ERROR_DETAIL = "ebms3ErrorDetail";

    String PULL_RECEIPT_REF_TO_MESSAGE_ID = "pullReceiptRefToMessageId";

    String getFinalRecipient(final String messageId);

    List<String> getFailedMessages(String finalRecipient);

    Long getFailedMessageElapsedTime(String messageId);

    void restoreFailedMessage(String messageId);

    void sendEnqueuedMessage(String messageId);

    /**
     * Resend a message in the status SEND_FAILURE or
     * SEND_ENQUEUED
     *
     * @param messageId message Id of the message
     */
    void resendFailedOrSendEnqueuedMessage(final String messageId);

    List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end, String finalRecipient);

    void deleteFailedMessage(String messageId);

    void deleteMessage(String messageId);

    /**
     * Schedules the handling of the SplitAndJoin send failed event
     *
     * @param groupId     The groupId for which the failure will be triggered
     * @param errorDetail The error detail
     */
    void scheduleSplitAndJoinSendFailed(String groupId, String errorDetail);

    /**
     * Schedules the marking of the UserMessageFragment as failed
     *
     * @param messageId
     */
    void scheduleSetUserMessageFragmentAsFailed(String messageId);

    /**
     * Schedules the sending of the SourceMessage
     *
     * @param messageId
     */
    void scheduleSourceMessageSending(String messageId);

    /**
     * Schedules the rejoining of the SourceMessage file
     *
     * @param groupId
     * @param backendName
     */
    void scheduleSourceMessageRejoinFile(String groupId, String backendName);

    /**
     * Schedules the rejoining of the SourceMessage
     *
     * @param groupId
     * @param file
     * @param backendName
     */
    void scheduleSourceMessageRejoin(String groupId, String file, String backendName);

    /**
     * Schedules the sending of the SourceMessage receipt
     *
     * @param messageId
     * @param pmodeKey
     */
    void scheduleSourceMessageReceipt(String messageId, String pmodeKey);

    /**
     * Schedules the sending of the Signal error in case a SourceMessage fails to be rejoined
     *
     * @param messageId
     * @param errorCode
     * @param errorDetail
     * @param pmodeKey
     */
    void scheduleSendingSignalError(String messageId, String errorCode, String errorDetail, String pmodeKey);

    void scheduleSplitAndJoinReceiveFailed(String groupId, String sourceMessageId, String errorCode, String errorDetail);

    void scheduleSending(String messageId, Long delay);

    /**
     * Schedule the sending of the asynchronous Pull Receipt
     *
     * @param messageId MessageId of the UserMessage (for which the pull receipt was generated)
     * @param pmodeKey  the pmode key of the UserMessage
     */
    void scheduleSendingPullReceipt(String messageId, String pmodeKey);

    /**
     * Schedule the sending of the asynchronous Pull Receipt (counting the retries)
     *
     * @param messageId  MessageId of the UserMessage (for which the pull receipt was generated)
     * @param pmodeKey   the pmode key of the UserMessage
     * @param retryCount the number of current attempts to send the receipt
     */
    void scheduleSendingPullReceipt(String messageId, String pmodeKey, int retryCount);

    /**
     * Gets a User Message based on the {@code messageId}
     *
     * @param messageId User Message Identifier
     * @return User Message {@link UserMessage}
     */
    UserMessage getMessage(String messageId);

    /**
     * Retrieves a message by id as a byte array
     * @param messageId
     * @return the message serialized as byte array
     * @throws MessageNotFoundException in case there is no message with this id
     */
    byte[] getMessageAsBytes(String messageId) throws MessageNotFoundException;

    /**
     * Retrieves the message content as a zip file(used for downloading a message)
     * @param messageId
     * @return a zip file with the message content
     * @throws MessageNotFoundException in case the message does nor exists or the content could not be retrieved( already sent and deleted)
     * @throws IOException in case a read error
     */
    byte[] getMessageWithAttachmentsAsZip(String messageId) throws MessageNotFoundException, IOException;
}
