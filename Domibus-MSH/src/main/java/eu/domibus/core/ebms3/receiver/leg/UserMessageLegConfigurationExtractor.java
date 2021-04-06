package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Loading legconfiguration for an incoming usermessage.
 */

public class UserMessageLegConfigurationExtractor extends AbstractLegConfigurationExtractor {

    private PModeProvider pModeProvider;
    private Ebms3Converter ebms3Converter;

    public UserMessageLegConfigurationExtractor(SoapMessage message, Ebms3Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration extractMessageConfiguration() throws EbMS3Exception {
        message.put(MSHDispatcher.MESSAGE_TYPE_IN, MessageType.USER_MESSAGE);
        final Messaging messaging = ebms3Converter.convertFromEbms3(ebms3Messaging);
        final String pmodeKey = this.pModeProvider.findUserMessageExchangeContext(messaging.getUserMessage(), MSHRole.RECEIVING).getPmodeKey(); // FIXME: This does not work for signalmessages
        setUpMessage(pmodeKey);
        return this.pModeProvider.getLegConfiguration(pmodeKey);
    }

    @Override
    public void accept(MessageLegConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    public void setpModeProvider(PModeProvider pModeProvider) {
        this.pModeProvider = pModeProvider;
    }

    public void setEbms3Converter(Ebms3Converter ebms3Converter) {
        this.ebms3Converter = ebms3Converter;
    }
}
