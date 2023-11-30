package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.api.message.UserMessageSoapEnvelopeSpiDelegate;
import eu.domibus.core.message.nonrepudiation.SaveRawEnvelopeInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import java.util.Collection;

/**
 * Interceptor intercept the outgoing messages
 *
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Service("messageOutgoingInterceptor")
public class MessageOutgoingInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageResponseSentBackendNotifierInterceptor.class);
    @Autowired
    protected UserMessageSoapEnvelopeSpiDelegate userMessageSoapEnvelopeSpiDelegate;

    public MessageOutgoingInterceptor() {
        super(Phase.WRITE_ENDING);
        addBefore(SaveRawEnvelopeInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        LOG.debug("Handling message");

        SOAPMessage originalMsg = message.getContent(SOAPMessage.class);

        Exchange exchange = message.getExchange();
        Collection<Attachment> attachments = null;
        if (exchange.getOutMessage() != null) {
            LOG.debug("Getting the attachments");
            attachments = exchange.getOutMessage().getAttachments();
        }

        userMessageSoapEnvelopeSpiDelegate.afterSigningAndEncryption(originalMsg, attachments);
    }

}
