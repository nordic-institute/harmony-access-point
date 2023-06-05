package eu.domibus.core.message.pull;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.ReceiptEntity;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.ReceiptDao;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.soap.SOAPMessage;

/**
 * @author idragusa
 * @since 4.1
 */
@SuppressWarnings("ConstantConditions")
@RunWith(JMockit.class)
public class PullReceiptListenerTest {

    @Tested
    private PullReceiptListener pullReceiptListener;

    @Injectable
    protected PullReceiptSender pullReceiptSender;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    private EbMS3MessageBuilder messageBuilder;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private UserMessageHandlerService userMessageHandlerService;

    @Injectable
    private UserMessageService userMessageService;

    @Injectable
    private ReceiptDao receiptDao;

    @Injectable
    private MessageStatusDao messageStatusDao;

    @Test
    public void onMessageTest_retry(@Injectable Message message, @Injectable ReceiptEntity receiptEntity) throws JMSException, EbMS3Exception {
        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = "mydomain";

            message.getStringProperty(UserMessageService.PULL_RECEIPT_REF_TO_MESSAGE_ID);
            result = "refToMessageId";

            message.getStringProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
            result = "pModeKey";

            receiptDao.findBySignalRefToMessageIdAndRole("refToMessageId", MSHRole.SENDING);
            result = null;

            message.propertyExists(MessageConstants.RETRY_COUNT);
            result = true;

            message.getIntProperty(MessageConstants.RETRY_COUNT);
            result = 1;
        }};

        pullReceiptListener.onMessage(message);

        new Verifications() {{
            pullReceiptSender.sendReceipt((SOAPMessage) any, anyString, (Policy) any,
                    (LegConfiguration) any, "pModeKey", "refToMessageId", "mydomain");
            times = 0;

            userMessageService.scheduleSendingPullReceipt("refToMessageId", "pModeKey", 2);
            times = 1;
        }};
    }

    @Test
    public void onMessageTest_maxRetry(@Injectable Message message, @Injectable ReceiptEntity receiptEntity) throws JMSException, EbMS3Exception {
        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = "mydomain";

            message.getStringProperty(UserMessageService.PULL_RECEIPT_REF_TO_MESSAGE_ID);
            result = "refToMessageId";

            message.getStringProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
            result = "pModeKey";

            receiptDao.findBySignalRefToMessageIdAndRole("refToMessageId", MSHRole.SENDING);
            result = receiptEntity;

            message.propertyExists(MessageConstants.RETRY_COUNT);
            result = true;

            message.getIntProperty(MessageConstants.RETRY_COUNT);
            result = 5;
        }};

        pullReceiptListener.onMessage(message);

        new Verifications() {{
            pullReceiptSender.sendReceipt((SOAPMessage) any, anyString, (Policy) any,
                    (LegConfiguration) any, "pModeKey", "refToMessageId", "mydomain");
            times = 1;

            userMessageService.scheduleSendingPullReceipt("refToMessageId", "pModeKey", 1);
            times = 0;
        }};
    }

    @Test
    public void onMessageTestNoRetry(@Injectable Message message) throws JMSException, EbMS3Exception {

        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = "mydomain";
            message.getStringProperty(UserMessageService.PULL_RECEIPT_REF_TO_MESSAGE_ID);
            result = "refToMessageId";
            message.getStringProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
            result = "pModeKey";

            receiptDao.findBySignalRefToMessageIdAndRole("refToMessageId", MSHRole.SENDING);
            result = null;
        }};

        pullReceiptListener.onMessage(message);

        new Verifications() {{
            userMessageService.scheduleSendingPullReceipt("refToMessageId", "pModeKey", 1);
            times = 1;
            pullReceiptSender.sendReceipt((SOAPMessage) any, anyString, (Policy) any,
                    (LegConfiguration) any, "pModeKey", "refToMessageId", "mydomain");
            times = 0;
        }};
    }
}
