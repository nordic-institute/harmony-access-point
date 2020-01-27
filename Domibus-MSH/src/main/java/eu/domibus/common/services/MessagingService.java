package eu.domibus.common.services;


import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.plugin.Submission;


/**
 * @author Ioana Dragusanu
 * @since 3.3
 */
public interface MessagingService {

    void storeMessage(Messaging messaging, MSHRole mshRole, final LegConfiguration legConfiguration, String backendName) throws CompressionException;

    void storePayloads(Messaging messaging, MSHRole mshRole, LegConfiguration legConfiguration, String backendName);

    void persistReceivedMessage(Messaging messaging, Messaging responseMessaging, BackendFilter matchingBackendFilter, UserMessage userMessage, String backendName, Party to, NotificationStatus notificationStatus) throws EbMS3Exception;

    void persistSubmittedMessage(Submission messageData, String backendName, UserMessage userMessage, String messageId, Messaging message, MessageExchangeConfiguration userMessageExchangeConfiguration, Party to, MessageStatus messageStatus, String pModeKey, LegConfiguration legConfiguration);

    void persistSubmittedMessageFragment(String backendName, UserMessage userMessage, String messageId, Messaging message, MessageExchangeConfiguration userMessageExchangeConfiguration, Party to, String pModeKey, LegConfiguration legConfiguration);
}
