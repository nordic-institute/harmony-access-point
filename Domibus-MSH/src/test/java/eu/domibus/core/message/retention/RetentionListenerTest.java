package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.ApplicationAuthenticatedProcedure;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Arrays;
import java.util.List;


import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RETENTION_WORKER_MESSAGE_ID_LIST_SEPARATOR;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */
@RunWith(JMockit.class)
public class RetentionListenerTest {

    @Tested
    private RetentionListener retentionListener;

    @Injectable
    private UserMessageDefaultService userMessageDefaultService;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Mocked
    private Message message;

    @Test
    public void onMessage_deletesMessage(@Mocked DomibusLogger domibusLogger) throws JMSException {
        // Given
        String messageId = "messageId";

        new Expectations() {{
            message.getStringProperty(MessageRetentionDefaultService.DELETE_TYPE); result = MessageDeleteType.SINGLE.name();
            message.getStringProperty(MessageConstants.MESSAGE_ID); result = messageId;
        }};

        // When
        retentionListener.onMessagePrivate(message);

        // Then
        new Verifications() {{
            domainContextProvider.setCurrentDomain(anyString);
            userMessageDefaultService.deleteMessage(messageId);
        }};
    }

    @Test
    public void onMessage_deletesMessageMulti(@Mocked DomibusLogger domibusLogger) throws JMSException {
        // Given
        List<String> messageIds = Arrays.asList("messageId1", "messageId2");

        new Expectations() {{
            message.getStringProperty(MessageRetentionDefaultService.DELETE_TYPE); result = MessageDeleteType.MULTI.name();
            message.getStringProperty(MessageRetentionDefaultService.MESSAGE_IDS); result = "messageId1,messageId2";
            domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_ID_LIST_SEPARATOR);
            result = ",";
        }};

        // When
        retentionListener.onMessagePrivate(message);

        // Then
        new Verifications() {{
            domainContextProvider.setCurrentDomain(anyString);
            userMessageDefaultService.deleteMessages(messageIds);
        }};
    }

    @Test(expected = IllegalArgumentException.class)
    public void onMessage_invalidDeleteType(@Mocked DomibusLogger domibusLogger) throws JMSException {

        new Expectations() {{
            message.getStringProperty(MessageRetentionDefaultService.DELETE_TYPE); result = "lulu";
        }};

        // When
        retentionListener.onMessagePrivate(message);

    }


    @Test
    public void onMessage_addsAuthentication(@Mocked DomibusLogger domibusLogger)  throws JMSException {
        // Given
        new Expectations() {{
            authUtils.runMethodWithSecurityContext((ApplicationAuthenticatedProcedure)any, anyString, anyString, (AuthRole)any);
        }};

        // When
        retentionListener.onMessage(message);

        // Then
        new FullVerifications() {{
            ApplicationAuthenticatedProcedure function;
            String username;
            String password;
            AuthRole role;
            authUtils.runMethodWithSecurityContext(function = withCapture(),
                    username=withCapture(), password=withCapture(), role=withCapture());
            Assert.assertNotNull(function);
            Assert.assertEquals("retention",username);
            Assert.assertEquals("retention",password);
            Assert.assertEquals(AuthRole.ROLE_ADMIN,role);
        }};
    }
}