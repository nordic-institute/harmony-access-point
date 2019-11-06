package eu.domibus.core.replication;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.UUID;


/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class UIReplicationSignalServiceImplTest {

    @Injectable
    private Queue uiReplicationQueue;

    @Injectable
    protected JMSManager jmsManager;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    UIReplicationSignalServiceImpl uiReplicationSignalService;

    private final String messageId = UUID.randomUUID().toString();

    @Test
    public void testIsReplicationEnabled() {

        new Expectations() {{
            domibusPropertyProvider.getProperty(UIReplicationSignalServiceImpl.UI_REPLICATION_ENABLED);
            result = true;
        }};

        //tested method
        Assert.assertTrue(uiReplicationSignalService.isReplicationEnabled());

    }

    @Test
    public void testUserMessageReceived(final @Mocked JmsMessage message) {

        new Expectations(uiReplicationSignalService) {{
           uiReplicationSignalService.isReplicationEnabled();
           result = true;

            uiReplicationSignalService.createJMSMessage(messageId, UIJMSType.USER_MESSAGE_RECEIVED);
            result = message;
        }};

        //tested method
        uiReplicationSignalService.userMessageReceived(messageId);

        new Verifications() {{
            jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
        }};
    }

    @Test
    public void testUserMessageSubmitted(final @Mocked JmsMessage message) {

        new Expectations(uiReplicationSignalService) {{
            uiReplicationSignalService.isReplicationEnabled();
            result = true;

            uiReplicationSignalService.createJMSMessage(messageId, UIJMSType.USER_MESSAGE_SUBMITTED);
            result = message;
        }};

        //tested method
        uiReplicationSignalService.userMessageSubmitted(messageId);

        new Verifications() {{
            jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
        }};
    }

    @Test
    public void testMessageChange(final @Mocked JmsMessage message) {
        new Expectations(uiReplicationSignalService) {{
            uiReplicationSignalService.isReplicationEnabled();
            result = true;

            uiReplicationSignalService.createJMSMessage(messageId, UIJMSType.MESSAGE_CHANGE);
            result = message;
        }};

        //tested method
        uiReplicationSignalService.messageChange(messageId);

        new Verifications() {{
            jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
        }};
    }

    @Test
    public void testSignalMessageSubmitted(final @Mocked JmsMessage message) {
        new Expectations(uiReplicationSignalService) {{
            uiReplicationSignalService.isReplicationEnabled();
            result = true;

            uiReplicationSignalService.createJMSMessage(messageId, UIJMSType.SIGNAL_MESSAGE_SUBMITTED);
            result = message;
        }};

        //tested method
        uiReplicationSignalService.signalMessageSubmitted(messageId);

        new Verifications() {{
            jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
        }};
    }

    @Test
    public void testSignalMessageReceived(final @Mocked JmsMessage message) {
        new Expectations(uiReplicationSignalService) {{
            uiReplicationSignalService.isReplicationEnabled();
            result = true;

            uiReplicationSignalService.createJMSMessage(messageId, UIJMSType.SIGNAL_MESSAGE_RECEIVED);
            result = message;
        }};

        //tested method
        uiReplicationSignalService.signalMessageReceived(messageId);

        new Verifications() {{
            jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
        }};
    }
}