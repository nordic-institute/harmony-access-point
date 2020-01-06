package eu.domibus.ebms3.sender;

import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPFault;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class SourceMessageSenderTest {

    @Tested
    private SourceMessageSender sourceMessageSender;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private MSHDispatcher mshDispatcher;

    @Injectable
    private EbMS3MessageBuilder messageBuilder;

    @Injectable
    private ReliabilityChecker reliabilityChecker;

    @Injectable
    private MessageAttemptService messageAttemptService;

    @Injectable
    private MessageExchangeService messageExchangeService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    protected SplitAndJoinService splitAndJoinService;

    @Injectable
    protected Messaging messaging;

    @Injectable
    protected UserMessageLog userMessageLog;

    @Injectable
    protected Domain currentDomain;

    @Injectable
    protected Runnable task;

    @Injectable
    protected UserMessage userMessage;

    @Injectable
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Test
    public void sendMessage() {

        new Expectations() {{
            domainContextProvider.getCurrentDomain();
            result = currentDomain;
            times = 1;
            sourceMessageSender.doSendMessage(userMessage, userMessageLog);
            times = 1;
        }};

        sourceMessageSender.sendMessage(messaging, userMessageLog);

    }

    @Test
    public void doSendMessageThrowsException(@Injectable MSHRole mshRole, @Injectable SOAPFault fault) throws EbMS3Exception {
        String pModeKey = "pModeKey";
        String messageId = "test";
        LegConfiguration legConfiguration = new LegConfiguration() {{
            setName("leg1");
        }};
        new NonStrictExpectations() {
            {
                userMessage.getMessageInfo().getMessageId();
                result = messageId;
                pModeProvider.findUserMessageExchangeContext(userMessage, mshRole.SENDING).getPmodeKey();
                result = pModeKey;
                pModeProvider.getLegConfiguration(pModeKey);
                result = legConfiguration;
                times = 1;
            }
        };

        sourceMessageSender.doSendMessage(userMessage, userMessageLog);
    }
}