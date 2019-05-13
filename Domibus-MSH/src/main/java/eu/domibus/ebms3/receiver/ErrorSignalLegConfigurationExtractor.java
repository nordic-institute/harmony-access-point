package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class ErrorSignalLegConfigurationExtractor extends AbstractSignalLegConfigurationExtractor {

    private MessagingDao messagingDao;

    private PModeProvider pModeProvider;

    private MessageExchangeService messageExchangeService;

    public ErrorSignalLegConfigurationExtractor(SoapMessage message, Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return messaging.getSignalMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration process() throws EbMS3Exception {
        Object pmode_key = message.getExchange().get(MessageBusProperty.PMODE_KEY.name());
        LOG.trace("Extracting pmode key:[{}] from synchronous error signal in order to load the policy.", pmode_key);
        if (pmode_key == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Pmode key for synchronous error signal is null", getMessageId(), null);
        }
        String refToMessageId = null;
        if (messaging.getSignalMessage().getMessageInfo() != null) {
            refToMessageId = messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
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


    public void setMessagingDao(MessagingDao messagingDao) {
        this.messagingDao = messagingDao;
    }

    public void setpModeProvider(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }

    public void setMessageExchangeService(MessageExchangeService messageExchangeService) {
        this.messageExchangeService = messageExchangeService;
    }
}
