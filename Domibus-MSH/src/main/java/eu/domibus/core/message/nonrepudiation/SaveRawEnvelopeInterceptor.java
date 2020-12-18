package eu.domibus.core.message.nonrepudiation;

import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * Interceptor to save the raw xml envelope of a signal message.
 * The non repudiation mechanism needs the raw message at the end of the interceptor queue,
 * as it needs the security header added
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class SaveRawEnvelopeInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveRawEnvelopeInterceptor.class);

    @Autowired
    NonRepudiationService nonRepudiationService;

    public SaveRawEnvelopeInterceptor() {
        super(Phase.WRITE_ENDING);
        addAfter(SoapOutInterceptor.SoapOutEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {

        LOG.debug("Entering save signal envelope method");

        final SOAPMessage jaxwsMessage = message.getContent(SOAPMessage.class);

        String userMessageId = (String) message.getExchange().get(DispatchClientDefaultProvider.EBMS_MESSAGE_ID);

        if (userMessageId != null) {
            nonRepudiationService.saveResponse(jaxwsMessage, userMessageId);
            LOG.debug("Saved the signal message envelope for user message id [{}]", userMessageId);
        }
    }

}