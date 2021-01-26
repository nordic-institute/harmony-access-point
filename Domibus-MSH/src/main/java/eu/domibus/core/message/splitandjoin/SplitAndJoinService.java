package eu.domibus.core.message.splitandjoin;

import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.api.model.Error;
import eu.domibus.api.model.UserMessage;

import javax.xml.soap.SOAPMessage;
import java.io.File;

/**
 * Class responsible for handling operations related to SplitAndJoin like: rejoin the source message based on message fragments, etc
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface SplitAndJoinService {

    void createUserFragmentsFromSourceFile(String sourceMessageFileName, SOAPMessage sourceMessageRequest, UserMessage userMessage, String contentTypeString, boolean compression);

    void sendSignalError(String messageId, String ebMS3ErrorCode, String errorDetail, String pmodeKey);

    /**
     * Checks if the leg is configured to use SplitAndJoin
     *
     * @param legConfiguration
     * @return
     */
    boolean mayUseSplitAndJoin(LegConfiguration legConfiguration);

    /**
     * Generates the file name for the source message
     *
     * @param temporaryDirectoryLocation
     * @return
     */
    String generateSourceFileName(String temporaryDirectoryLocation);

    /**
     * Rejoins the source message file from the message fragments associated to a specific group
     *
     * @param groupId
     * @return
     */
    File rejoinMessageFragments(String groupId);

    SOAPMessage getUserMessage(File sourceMessageFileName, String contentTypeString);

    /**
     * Rejoins the source message from a file present on disk
     *
     * @param groupId
     * @param sourceMessageFile
     * @param backendName
     */
    void rejoinSourceMessage(String groupId, String sourceMessageFile, String backendName);

    /**
     * Marks the SourceMessage as failed
     *
     * @param userMessage The SourceMessage to be marked as failed
     */
    void setSourceMessageAsFailed(final UserMessage userMessage);

    /**
     * Marks a specific MessageFragment as failed
     *
     * @param messageId
     */
    void setUserMessageFragmentAsFailed(String messageId);

    /**
     * Marks all the MessageFragment messages associated to a specific group as failed, including the SourceMessage
     *
     * @param groupId The groupId for which the failure will be performed
     * @param errorDetail The error detail
     */
    void splitAndJoinSendFailed(final String groupId, final String errorDetail);

    /**
     * Handles the Signal error received from C3 which indicates that there was an error while rejoining the SourceMessage
     *
     * @param messageId The message ID of the SourceMessage
     * @param error     The details of the occurred error
     */
    void handleSourceMessageSignalError(final String messageId, final Error error);

    /**
     * Generates and sends the receipt associated to the SourceMessage
     *
     * @param sourceMessageId The message ID of the SourceMessage
     * @param pModeKey        The PMode key to used
     */
    void sendSourceMessageReceipt(String sourceMessageId, String pModeKey);

    /**
     * Marks all the MessageFragment messages associated to a specific group as failed, including the SourceMessage
     *
     * @param groupId
     * @param sourceMessageId
     * @param ebMS3ErrorCode
     * @param errorDetail
     */
    void splitAndJoinReceiveFailed(String groupId, final String sourceMessageId, String ebMS3ErrorCode, String errorDetail);

    /**
     * Mark the sent/received SplitAndJoin messages as expired based on the joinInterval configuration
     */
    void handleExpiredGroups();

    void incrementSentFragments(String groupId);

    void incrementReceivedFragments(String groupId, String backendName);
}
