package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.api.model.Messaging;

import javax.xml.soap.SOAPMessage;

/**
 * Defines the contract for message handlers responsible for handling incoming AS4 messages
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface IncomingMessageHandler {

    SOAPMessage processMessage(final SOAPMessage request, final Messaging messaging) throws EbMS3Exception;
}
