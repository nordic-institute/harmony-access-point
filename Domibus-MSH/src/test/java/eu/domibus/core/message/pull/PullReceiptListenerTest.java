package eu.domibus.core.message.pull;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.Receipt;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author idragusa
 * @since 4.1
 */
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
    UserMessageService userMessageService;

    @Test
    public void onMessageTest(@Mocked Message message) throws JMSException, EbMS3Exception {


        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = "mydomain";

            signalMessageDao.findSignalMessagesByRefMessageId(anyString);
            result = createSignalMessages();
        }};

        pullReceiptListener.onMessage(message);

        new Verifications() {{
            pullReceiptSender.sendReceipt((SOAPMessage) any, anyString, (Policy) any,
                    (LegConfiguration) any, anyString, anyString, anyString);
            times = 1;
        }};
    }

    @Test
    public void onMessageTestNoReceipt(@Mocked Message message) throws JMSException, EbMS3Exception {

        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = "mydomain";

            signalMessageDao.findSignalMessagesByRefMessageId(anyString);
            result = null;
        }};

        pullReceiptListener.onMessage(message);

        new Verifications() {{
            pullReceiptSender.sendReceipt((SOAPMessage) any, anyString, (Policy) any,
                    (LegConfiguration) any, anyString, anyString, anyString);
            times = 0;
        }};
    }

    private List<SignalMessage> createSignalMessages() {
        List<SignalMessage> signalMessages = new ArrayList<>();
        SignalMessage signalMessage = new SignalMessage();
        Receipt receipt = new Receipt();
        List<String> anyReceipt = new ArrayList<>();
        anyReceipt.add("some content for the receipt");
        receipt.setAny(anyReceipt);
        signalMessage.setReceipt(receipt);
        signalMessages.add(signalMessage);
        return signalMessages;
    }
}