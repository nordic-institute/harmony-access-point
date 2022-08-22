
package eu.domibus.plugin;

import eu.domibus.common.*;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import java.util.List;

/**
 * Definition of a backend integration plugin. Direct implementation of this interface is NOT RECOMMENDED.
 * Implementations should extend eu.domibus.plugin.AbstractBackendConnector instead.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public interface BackendConnector<U, T> {

    /**
     * @return the MessageSubmissionTransformer matching the intended backend submission DTO
     */
    MessageSubmissionTransformer<U> getMessageSubmissionTransformer();

    /**
     * @return MessageRetrievalTransformer matching the intended backend retrieval DTO
     */
    MessageRetrievalTransformer<T> getMessageRetrievalTransformer();


    /**
     * Submits a message in the native format to the Domibus MSH
     *
     * @param message the message to be sent
     * @return the Domibus-internal Id of the message. This Id is used for all further reference to the submitted message.
     * @throws MessagingProcessingException If the message was rejected by the Domibus MSH
     */
    String submit(final U message) throws MessagingProcessingException;

    /**
     * provides the message with the corresponding messageId. A target object (i.e. an instance of javax.jms.Message)
     * can be provided. This is necessary in case the DTO for transfer to the backend is constructed by a
     * factory (i.e. a JMS session). Persists the message state as downloaded, and we only downloads messages that were not already downloaded.
     *
     * @param messageId the messageId of the message to retrieve
     * @param target    the target object to be filled.
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message was not found
     */
    T downloadMessage(final String messageId, final T target) throws MessageNotFoundException;

    /**
     * provides the message with the corresponding messageId. A target object (i.e. an instance of javax.jms.Message)
     * can be provided. This is necessary in case the DTO for transfer to the backend is constructed by a
     * factory (i.e. a JMS session).
     *
     * @param messageId        the messageId of the message to retrieve
     * @param target           the target object to be filled.
     * @param markAsDownloaded if true then we persist the message state as downloaded, and we only download messages that were not already downloaded
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message was not found
     */
    T downloadMessage(final String messageId, final T target, boolean markAsDownloaded) throws MessageNotFoundException;

    /**
     * provides the message with the corresponding messageId. A target object (i.e. an instance of javax.jms.Message)
     * can be provided. This is necessary in case the DTO for transfer to the backend is constructed by a
     * factory (i.e. a JMS session).
     *
     * @param messageEntityId the entity id of the message to retrieve
     * @param target    the target object to be filled.
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message was not found
     */
    T downloadMessage(final Long messageEntityId, final T target) throws MessageNotFoundException;

    /**
     * Validates the message status and marks it as downloaded
     * @param messageId the messageId of the message to browse
     * @throws MessageNotFoundException if the message was not found
     */
    void markMessageAsDownloaded(final String messageId) throws MessageNotFoundException;
    /**
     * Browses the message with the corresponding messageId. A target object (i.e. an instance of javax.jms.Message)
     * can be provided. This is necessary in case the DTO for transfer to the backend is constructed by a
     * factory (i.e. a JMS session).
     *
     * @param messageId the messageId of the message to browse
     * @param target    the target object to be filled.
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message was not found
     */
    T browseMessage(final String messageId, final T target) throws MessageNotFoundException;

    T browseMessage(final String messageId, final MSHRole mshRole, final T target) throws MessageNotFoundException;

    /**
     * Browses the message with the corresponding messageId. A target object (i.e. an instance of javax.jms.Message)
     * can be provided. This is necessary in case the DTO for transfer to the backend is constructed by a
     * factory (i.e. a JMS session).
     *
     * @param messageEntityId the entity id of the message to browse
     * @param target    the target object to be filled.
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message was not found
     */
    T browseMessage(final Long messageEntityId, final T target) throws MessageNotFoundException;

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageId id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     * @deprecated since 5.1 Use instead {@link #getStatus(String messageId, MSHRole role)}
     */
    @Deprecated
    MessageStatus getStatus(final String messageId);

    MessageStatus getStatus(final String messageId, final MSHRole mshRole);

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageEntityId entity id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     */
    MessageStatus getStatus(final Long messageEntityId);

    /**
     * Returns List {@link java.util.List} of error logs {@link ErrorResult} for message with messageid
     *
     * @param messageId id of the message the errors are requested for
     * @return the list of error log entries {@link java.util.List} of {@link ErrorResult}
     * @deprecated since 5.1 Use instead {@link #getErrorsForMessage(String messageId, MSHRole role)}
     */
    @Deprecated
    List<ErrorResult> getErrorsForMessage(final String messageId);

    List<ErrorResult> getErrorsForMessage(final String messageId, final MSHRole mshRole);

    /**
     * Delivers the message with the associated messageId to the backend application. Plugins MUST OVERRIDE this method.
     *
     * @param event containing details about the deliver message event
     */
    void deliverMessage(final DeliverMessageEvent event);

    /**
     * Initiates a pull request for the given mpc
     *
     * @param mpc the MPC to be used in the pull request
     */
    void initiatePull(final String mpc);

    /**
     * The name of the plugin instance. To allow for multiple deployments of the same plugin this value should be externalized.
     *
     * @return the name of the plugin
     */
    String getName();

    /**
     * Configured notifications sent to the plugin, depending on their MODE (PULL or PUSH)
     *
     * @return the plugin notifications
     */
    default List<NotificationType> getRequiredNotifications() {
        return NotificationType.DEFAULT_PUSH_NOTIFICATIONS;
    }

    /**
     * Custom action to be performed by the plugins when a message is being deleted
     *
     * @param event The message delete event details eg message id
     */
    default void messageDeletedEvent(MessageDeletedEvent event) {
    }


    /**
     * Custom action to be performed by the plugins when a batch of messages are being deleted
     *
     * @param event The message delete batch event details eg list of message ids
     */
    default void messageDeletedBatchEvent(MessageDeletedBatchEvent event) {
    }

    /**
     * This method gets called when an incoming message is rejected by the MSH
     *
     * @param event event containing details about the message receive failure event
     */
    void messageReceiveFailed(MessageReceiveFailureEvent event);

    /**
     * This method gets called when the status of a User Message changes
     *
     * @param event event containing details about the message status change event
     */
    void messageStatusChanged(MessageStatusChangeEvent event);

    /**
     * Notifies the plugins for every payload that has been submitted to C2 but not yet saved
     *
     * @param event The event containing the details of the payload submitted event
     */
    void payloadSubmittedEvent(PayloadSubmittedEvent event);

    /**
     * Notifies the plugins for every payload that has been saved by C2
     *
     * @param event The event containing the details of the payload processed event
     */
    void payloadProcessedEvent(PayloadProcessedEvent event);

    /**
     * This method gets called when an outgoing message associated with an associated
     * PMode[1].errorHandling.Report.ProcessErrorNotifyProducer=true has finally failed to be delivered. The error details
     * are provided by #getErrorsForMessage. This is only called for messages that have no retry attempts left.
     *
     * @param event The event containing the details of the send failed event
     */
    void messageSendFailed(MessageSendFailedEvent event);

    /**
     * This method gets called when an outgoing message has been successfully sent
     * to the intended receiving MSH
     *
     * @param event The event containing the details of the message send success event
     */
    void messageSendSuccess(final MessageSendSuccessEvent event);

    /**
     * Describes the message exchange protocol
     */
    enum Mep {
        /**
         * Exchange is ONE WAY. Only one message user exchanged.
         */
        ONE_WAY("oneway"),
        /**
         * Exchange of multiple UserMessage.
         */
        TWO_WAY("twoway");


        private final String fileMapping;

        Mep(String fileMapping) {
            this.fileMapping = fileMapping;
        }

        public String getFileMapping() {
            return fileMapping;
        }
    }
}
