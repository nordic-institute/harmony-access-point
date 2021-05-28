package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 4.1
 */

public class ErrorSignalLegConfigurationExtractor extends AbstractSignalLegConfigurationExtractor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorSignalLegConfigurationExtractor.class);

    private PModeProvider pModeProvider;

    public ErrorSignalLegConfigurationExtractor(SoapMessage message, Ebms3Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return ebms3Messaging.getSignalMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration process() throws EbMS3Exception {
        Object pmode_key = message.getExchange().get(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
        LOG.trace("Extracting pmode key:[{}] from synchronous error signal in order to load the policy.", pmode_key);
        if (pmode_key == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Pmode key for synchronous error signal is null", getMessageId(), null);
        }
        String refToMessageId = null;
        if (ebms3Messaging.getSignalMessage().getMessageInfo() != null) {
            refToMessageId = ebms3Messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
        }

        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration((String) pmode_key);
        if (legConfiguration == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, String.format("Legconfiguration for synchronous error signal with pmode key:[%s] is null", pmode_key), refToMessageId, null);
        }
        return legConfiguration;
    }

    @Override
    public void accept(MessageLegConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    public void setpModeProvider(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }
}
