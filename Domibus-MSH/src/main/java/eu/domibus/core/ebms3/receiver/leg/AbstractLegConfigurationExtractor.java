package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public abstract class AbstractLegConfigurationExtractor implements LegConfigurationExtractor {

    protected final SoapMessage message;

    protected final Ebms3Messaging ebms3Messaging;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractLegConfigurationExtractor.class);


    public AbstractLegConfigurationExtractor(final SoapMessage message, final Ebms3Messaging ebms3Messaging) {
        this.message = message;
        this.ebms3Messaging = ebms3Messaging;
    }


    protected abstract String getMessageId();


    public void setUpMessage(final String pmodeKey) {
        //set the messageId in the MDC context
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, getMessageId());
        message.getExchange().put(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY, getMessageId());
        message.put(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
        //FIXME: Test!!!!
        message.getExchange().put(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
    }


}
