package eu.domibus.core.message;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class UserMessageSecurityDefaultServiceTest {

    @Tested
    UserMessageSecurityDefaultService userMessageSecurityDefaultService;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    UserMessageServiceHelper userMessageServiceHelper;


    @Test(expected = AuthenticationException.class)
    public void testCheckMessageAuthorizationWithNonExistingMessage() {
        final String messageId = "1";
        new Expectations() {{
            userMessageService.findByMessageId(messageId);
            result = null;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
    }

    @Test
    public void testCheckMessageAuthorizationWithExistingMessage(@Injectable UserMessage userMessage) {
        final String messageId = "1";
        new Expectations(userMessageSecurityDefaultService) {{
            userMessageService.findByMessageId(messageId);
            result = userMessage;

            userMessageSecurityDefaultService.validateUserAccessWithUnsecureLoginAllowed(userMessage);
            times = 1;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);

    }

    @Test
    public void testCheckAuthorizationWithAdminRole(final @Capturing DomibusLogger log) {
        final String finalRecipient = "C4";
        new Expectations() {{
            authUtils.getOriginalUser();
            result = null;
        }};

        userMessageSecurityDefaultService.checkAuthorization(finalRecipient);

        new Verifications() {{
            log.debug("finalRecipient from the security context is empty, user has permission to access finalRecipient [{}]", finalRecipient);
            times = 1;
        }};
    }

    @Test(expected = AuthenticationException.class)
    public void testCheckSecurityWhenOriginalUserFromSecurityContextIsDifferent() {
        final String finalRecipient = "C4";
        final String originalUserFromSecurityContext = "differentRecipient";

        new Expectations() {{
            authUtils.getOriginalUser();
            result = originalUserFromSecurityContext;
        }};

        userMessageSecurityDefaultService.checkAuthorization(finalRecipient);
    }

    @Test
    public void testCheckSecurityWhenOriginalUserFromSecurityContextIsSame() {
        final String finalRecipient = "C4";
        final String originalUserFromSecurityContext = "C4";

        new Expectations() {{
            authUtils.getOriginalUser();
            result = originalUserFromSecurityContext;
        }};

        userMessageSecurityDefaultService.checkAuthorization(finalRecipient);
    }


    @Test
    public void testValidateOriginalUserOK_finalRecipient(@Injectable final UserMessage userMessage) {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
        String other = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUser();
            result = originalUser;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
            result = other;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.FINAL_RECIPIENT);
            result = originalUser;
        }};

        userMessageSecurityDefaultService.validateUserAccessWithUnsecureLoginAllowed(userMessage);
    }

    @Test
    public void testValidateOriginalUserOK_originalSender(@Injectable final UserMessage userMessage) {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
        String other = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUser();
            result = originalUser;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
            result = originalUser;
        }};

        userMessageSecurityDefaultService.validateUserAccessWithUnsecureLoginAllowed(userMessage);
    }

    @Test
    public void testValidateOriginalUserOK_admin(@Injectable final UserMessage userMessage) {

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUser();
            result = null;
        }};

        userMessageSecurityDefaultService.validateUserAccessWithUnsecureLoginAllowed(userMessage);
    }

    @Test(expected = AccessDeniedException.class)
    public void validateUserAccess_noAccess(@Injectable final UserMessage userMessage) {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
        String other = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUser();
            result = originalUser;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
            result = other;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.FINAL_RECIPIENT);
            result = other;
        }};

        userMessageSecurityDefaultService.validateUserAccessWithUnsecureLoginAllowed(userMessage);
    }

}
