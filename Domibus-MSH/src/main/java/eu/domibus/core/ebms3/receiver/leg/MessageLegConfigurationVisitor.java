package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.pull.PullRequestLegConfigurationExtractor;
import eu.domibus.core.pmode.provider.PModeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Component
public class MessageLegConfigurationVisitor {

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    protected Ebms3Converter ebms3Converter;

    public void visit(UserMessageLegConfigurationExtractor userMessagePolicyInSetup) {
        userMessagePolicyInSetup.setpModeProvider(pModeProvider);
        userMessagePolicyInSetup.setEbms3Converter(ebms3Converter);
    }

    public void visit(PullRequestLegConfigurationExtractor signalMessagePolicyInSetup) {
        signalMessagePolicyInSetup.setMessageExchangeService(messageExchangeService);
    }

    public void visit(ReceiptLegConfigurationExtractor receiptMessagePolicyInSetup) {
        receiptMessagePolicyInSetup.setUserMessageDao(userMessageDao);
        receiptMessagePolicyInSetup.setpModeProvider(pModeProvider);
        receiptMessagePolicyInSetup.setMessageExchangeService(messageExchangeService);
    }

    public void visit(ErrorSignalLegConfigurationExtractor errorSignalLegConfigurationExtractor) {
        errorSignalLegConfigurationExtractor.setpModeProvider(pModeProvider);
    }
}
