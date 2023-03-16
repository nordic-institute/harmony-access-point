package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MessageStatus;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.handler.MessageRetriever;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class MessageRetrieverServiceDelegateTest {
    private static final String MESS_ID = UUID.randomUUID().toString();

    @Tested
    MessageRetrieverServiceDelegate messageRetrieverServiceDelegate;

    @Injectable
    private MessageRetriever messageRetriever;

    @Injectable
    private UserMessageSecurityService userMessageSecurityService;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private UserMessageService userMessageService;

    @Test
    public void testDownloadMessageAuthUserNok(@Injectable UserMessage userMessage, @Injectable String messageId) {

        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
        new Expectations(messageRetrieverServiceDelegate) {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            userMessageService.getByMessageId(messageId);
            result = userMessage;

            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(userMessage, MessageConstants.FINAL_RECIPIENT);
            result = new AuthenticationException("You are not allowed to handle this message");
        }};

        try {
            messageRetrieverServiceDelegate.checkMessageAuthorization(messageId);
            Assert.fail("It should throw AuthenticationException");
        } catch (AuthenticationException adEx) {
            assertTrue(adEx.getMessage().contains("You are not allowed to handle this message"));
        }

        new Verifications() {{
            authUtils.checkHasAdminRoleOrUserRoleWithOriginalUser();
        }};

    }

    @Test
    public void testGetStatus() throws MessageNotFoundException, DuplicateMessageException {
        // Given
        new Expectations() {{
            messageRetriever.getStatus(MESS_ID);
            result = MessageStatus.ACKNOWLEDGED;
        }};

        // When
        final eu.domibus.common.MessageStatus status = messageRetrieverServiceDelegate.getStatus(MESS_ID);

        Assert.assertEquals(eu.domibus.common.MessageStatus.ACKNOWLEDGED, status);

        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(MESS_ID);
            times = 1;
        }};

    }

    @Test
    public void testGetStatusAccessDenied() throws MessageNotFoundException, DuplicateMessageException {
        // Given
        new Expectations() {{
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(MESS_ID);
            result = new AuthenticationException("");
        }};

        // When
        eu.domibus.common.MessageStatus status = null;
        try {
            status = messageRetrieverServiceDelegate.getStatus(MESS_ID);
            Assert.fail("It should throw AuthenticationException");
        } catch (AuthenticationException ex) {
            // ok
        }

        Assert.assertNull(status);

    }
}
