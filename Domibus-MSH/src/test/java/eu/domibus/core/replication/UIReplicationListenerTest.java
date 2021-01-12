package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.common.MessageStatus;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.MapMessage;
import java.util.UUID;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class UIReplicationListenerTest {

    @Injectable
    private UIReplicationDataService uiReplicationDataService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Tested
    UIReplicationListener uiReplicationListener;

    @Test
    public void testProcessUIReplication_UIReplicationEnabled(final @Mocked MapMessage mapMessage) throws Exception {
        final String domainCode = "DEFAULT";
        final String messageId = UUID.randomUUID().toString();
        final long jmsTimestamp = 123423546378L;
        final MessageStatus messageStatus = MessageStatus.DOWNLOADED;
        final NotificationStatus notificationStatus = NotificationStatus.NOTIFIED;

        new Expectations() {{
            mapMessage.getStringProperty(MessageConstants.DOMAIN);
            result = domainCode;

            uiReplicationSignalService.isReplicationEnabled();
            result = true;

            mapMessage.getStringProperty(MessageConstants.MESSAGE_ID);
            result = messageId;

            mapMessage.getJMSType();
            result = UIJMSType.USER_MESSAGE_RECEIVED.name();
            result = UIJMSType.USER_MESSAGE_SUBMITTED.name();
            result = UIJMSType.MESSAGE_CHANGE.name();
            result = UIJMSType.SIGNAL_MESSAGE_SUBMITTED.name();
            result = UIJMSType.SIGNAL_MESSAGE_RECEIVED.name();
            result = "blah";

            mapMessage.getJMSTimestamp();
            result = jmsTimestamp;
        }};

        //tested method
        uiReplicationListener.processUIReplication(mapMessage);
        uiReplicationListener.processUIReplication(mapMessage);
        uiReplicationListener.processUIReplication(mapMessage);
        uiReplicationListener.processUIReplication(mapMessage);
        uiReplicationListener.processUIReplication(mapMessage);
        try {
            uiReplicationListener.processUIReplication(mapMessage);
        } catch (Exception e) {
            Assert.assertEquals(IllegalArgumentException.class, e.getClass());
        }

        new FullVerifications() {{
            String domainCodeActual;
            domainContextProvider.setCurrentDomain(domainCodeActual = withCapture());
            Assert.assertEquals(domainCode, domainCodeActual);

            databaseUtil.getDatabaseUserName();

            long jmsTimestampActual;
            uiReplicationDataService.userMessageReceived(messageId, jmsTimestampActual = withCapture());
            Assert.assertEquals(jmsTimestamp, jmsTimestampActual);

            uiReplicationDataService.userMessageSubmitted(messageId, jmsTimestampActual = withCapture());
            Assert.assertEquals(jmsTimestamp, jmsTimestampActual);

            uiReplicationDataService.messageChange(messageId, jmsTimestampActual = withCapture());
            Assert.assertEquals(jmsTimestamp, jmsTimestampActual);

            uiReplicationDataService.signalMessageSubmitted(messageId, jmsTimestampActual = withCapture());
            Assert.assertEquals(jmsTimestamp, jmsTimestampActual);

            uiReplicationDataService.signalMessageReceived(messageId, jmsTimestampActual = withCapture());
            Assert.assertEquals(jmsTimestamp, jmsTimestampActual);
        }};
    }


    @Test
    public void testProcessUIReplication_UIReplicationDisabled(final @Mocked MapMessage mapMessage) throws Exception {
        final String domainCode = "DEFAULT";

        new Expectations() {{
            mapMessage.getStringProperty(MessageConstants.DOMAIN);
            result = domainCode;

            uiReplicationSignalService.isReplicationEnabled();
            result = false;

        }};

        //tested method
        uiReplicationListener.processUIReplication(mapMessage);

        new FullVerifications() {{
            String domainCodeActual;
            domainContextProvider.setCurrentDomain(domainCodeActual = withCapture());
            Assert.assertEquals(domainCode, domainCodeActual);
            databaseUtil.getDatabaseUserName();
        }};
    }
}