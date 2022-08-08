
package eu.domibus.plugin.handler;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;

import java.util.List;

/**
 * Implementation of this interface handles the retrieval of messages from Domibus to the backend.
 *
 * @author Christian Koch, Stefan Mueller, Cosmin Baciu
 * @since 3.0
 */
public interface MessageRetriever {

    /**
     * provides the message with the corresponding messageId
     *
     * @param messageId the messageId of the message to retrieve
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message could not be found
     */
    Submission downloadMessage(String messageId) throws MessageNotFoundException;

    /**
     * provides the message with the corresponding messageId
     *
     * @param messageEntityId the entity id of the message to retrieve
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message could not be found
     */
    Submission downloadMessage(Long messageEntityId) throws MessageNotFoundException;

    /**
     * Browse the message with the corresponding messageId
     *
     * @param messageId the messageId of the message to browse
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message could not be found
     * @deprecated since 5.1 Use instead {@link #browseMessage(String messageId, MSHRole role)}
     */
    @Deprecated
    Submission browseMessage(String messageId) throws MessageNotFoundException;

    Submission browseMessage(String messageId, MSHRole mshRole) throws MessageNotFoundException;

    /**
     * Browse the message with the corresponding messageId
     *
     * @param messageEntityId the entity id of the message to browse
     * @return the message object with the given messageId
     * @throws MessageNotFoundException if the message could not be found
     */
    Submission browseMessage(Long messageEntityId) throws MessageNotFoundException;

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageId id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     * @deprecated since 5.1 Use instead {@link #getStatus(String messageId, MSHRole role)}
     */
    @Deprecated
    MessageStatus getStatus(String messageId);

    MessageStatus getStatus(String messageId, MSHRole mshRole);

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
    List<? extends ErrorResult> getErrorsForMessage(String messageId);

    List<? extends ErrorResult> getErrorsForMessage(String messageId, MSHRole mshRole);
}
