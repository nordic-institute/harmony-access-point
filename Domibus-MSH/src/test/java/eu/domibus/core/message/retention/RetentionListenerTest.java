package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.retention.RetentionListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

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
        retentionListener.onMessage(message);

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
        retentionListener.onMessage(message);

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
        retentionListener.onMessage(message);

    }


    @Test
    public void onMessage_addsAuthentication(@Mocked DomibusLogger domibusLogger)  throws JMSException {
        // Given
        new Expectations() {{
            authUtils.isUnsecureLoginAllowed(); result = false;
            message.getStringProperty(MessageRetentionDefaultService.DELETE_TYPE); result = MessageDeleteType.SINGLE.name();
        }};

        // When
        retentionListener.onMessage(message);

        // Then
        new Verifications() {{
            authUtils.setAuthenticationToSecurityContext(anyString, anyString, AuthRole.ROLE_ADMIN);
        }};
    }
}