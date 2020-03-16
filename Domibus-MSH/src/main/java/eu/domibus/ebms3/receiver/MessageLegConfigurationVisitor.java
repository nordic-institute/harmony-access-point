package eu.domibus.ebms3.receiver;

import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.MessageExchangeService;
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
    private MessagingDao messagingDao;

    void visit(UserMessageLegConfigurationExtractor userMessagePolicyInSetup) {
        userMessagePolicyInSetup.setpModeProvider(pModeProvider);
    }

    void visit(PullRequestLegConfigurationExtractor signalMessagePolicyInSetup) {
        signalMessagePolicyInSetup.setMessageExchangeService(messageExchangeService);
    }

    void visit(ReceiptLegConfigurationExtractor receiptMessagePolicyInSetup) {
        receiptMessagePolicyInSetup.setMessagingDao(messagingDao);
        receiptMessagePolicyInSetup.setpModeProvider(pModeProvider);
        receiptMessagePolicyInSetup.setMessageExchangeService(messageExchangeService);
    }

    void visit(ErrorSignalLegConfigurationExtractor errorSignalLegConfigurationExtractor) {
        errorSignalLegConfigurationExtractor.setpModeProvider(pModeProvider);
    }
}
