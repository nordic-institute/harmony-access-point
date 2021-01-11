package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDto;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;
import java.lang.reflect.Type;
import java.util.List;


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

    @Injectable
    JsonUtil jsonUtil;

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
            authUtils.runWithSecurityContext((AuthenticatedProcedure)any, anyString, anyString, (AuthRole)any);
        }};

        // When
        retentionListener.onMessage(message);

        // Then
        new FullVerifications() {{
            AuthenticatedProcedure function;
            String username;
            String password;
            AuthRole role;
            authUtils.runWithSecurityContext(function = withCapture(),
                    username=withCapture(), password=withCapture(), role=withCapture());
            Assert.assertNotNull(function);
            Assert.assertEquals("retention",username);
            Assert.assertEquals("retention",password);
            Assert.assertEquals(AuthRole.ROLE_ADMIN,role);
        }};
    }
}