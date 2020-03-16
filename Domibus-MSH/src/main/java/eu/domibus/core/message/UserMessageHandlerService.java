package eu.domibus.core.message;

import eu.domibus.common.ErrorResult;
import eu.domibus.core.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface UserMessageHandlerService {
    /**
     * to be appended to messageId when saving to DB on receiver side
     */
    String SELF_SENDING_SUFFIX = "_1";

    /**
     * Handles incoming UserMessages
     *
     * @param legConfiguration
     * @param pmodeKey
     * @param request
     * @param messaging
     * @param testMessage
     * @return
     * @throws EbMS3Exception
     * @throws TransformerException
     * @throws IOException
     * @throws JAXBException
     * @throws SOAPException
     */
    SOAPMessage handleNewUserMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;

    /**
     * Handles incoming source messages for SplitAndJoin
     *
     * @param legConfiguration
     * @param pmodeKey
     * @param request
     * @param messaging
     * @param testMessage
     * @return
     * @throws EbMS3Exception
     * @throws TransformerException
     * @throws IOException
     * @throws JAXBException
     * @throws SOAPException
     */
    SOAPMessage handleNewSourceUserMessage(LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException;

    /**
     * Checks if this message is a test message.
     *
     * @param message the message
     * @return result of test service and action handle
     */

    Boolean checkTestMessage(UserMessage message);

    /**
     * Checks if this message is a test message.
     *
     * @param service the service of this user message
     * @param action the action of this user message
     *
     * @return result of test service and action handle
     */
    Boolean checkTestMessage(String service, String action);

    /**
     * Checks if this message is a test message.
     *
     * @param legConfiguration the legConfiguration that matched the message
     * @return result of test service and action handle
     */

    Boolean checkTestMessage(final LegConfiguration legConfiguration);

    Boolean checkSelfSending(String pmodeKey);

    ErrorResult createErrorResult(EbMS3Exception ebm3Exception);

    /**
     * Add the payloads from the UserMessage as attachments of the SOAPMessage
     * @param request The SOAPMessage to which attachments are added
     * @param userMessage The UserMessage that contains the payloads
     * @throws EbMS3Exception in case there are validation errors
     * @throws SOAPException
     * @throws TransformerException
     */
    void handlePayloads(SOAPMessage request, UserMessage userMessage) throws EbMS3Exception, SOAPException, TransformerException;
}
