package eu.domibus.core.message.receipt;

import eu.domibus.api.model.SignalMessageResult;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.ReplyPattern;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Class responsible for generating AS4 receipts
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface AS4ReceiptService {

    /**
     * Generates AS4 receipt based on a SOAPMessage request
     *
     * @param request
     * @param userMessage
     * @param replyPattern
     * @param nonRepudiation
     * @param duplicate
     * @param selfSendingFlag
     * @return
     * @throws EbMS3Exception
     */
    SOAPMessage generateReceipt(SOAPMessage request,
                                UserMessage userMessage,
                                ReplyPattern replyPattern,
                                Boolean nonRepudiation,
                                Boolean duplicate,
                                Boolean selfSendingFlag) throws EbMS3Exception;

    /**
     * Generates AS4 receipt based on an already received request
     *
     * @param messageId
     * @param nonRepudiation
     * @return
     * @throws EbMS3Exception
     */
    SOAPMessage generateReceipt(String messageId, final Boolean nonRepudiation) throws EbMS3Exception;

    SignalMessageResult generateResponse(SOAPMessage responseMessage, boolean selfSendingFlag) throws EbMS3Exception, SOAPException;
}
