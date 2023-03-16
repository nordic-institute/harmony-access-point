package eu.domibus.core.message;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    UserMessageDao userMessageDao;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    UserMessageServiceHelper userMessageServiceHelper;


    @Test(expected = MessageNotFoundException.class)
    public void testCheckMessageAuthorizationWithNonExistingMessage() {
        final String messageId = "1";
        new Expectations() {{
            userMessageDao.findByMessageId(messageId, MSHRole.RECEIVING);
            result = null;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, MSHRole.RECEIVING);
    }

    @Test
    public void testCheckMessageAuthorizationWithExistingMessage(@Injectable UserMessage userMessage) {
        final String messageId = "1";
        new Expectations(userMessageSecurityDefaultService) {{
            userMessageDao.findByMessageId(messageId, MSHRole.RECEIVING);
            result = userMessage;

            userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(userMessage);
            times = 1;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, MSHRole.RECEIVING);

    }

    @Test
    public void testValidateOriginalUserOK_finalRecipient(@Injectable final UserMessage userMessage) {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
        String other = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserOrNullIfAdmin();
            result = originalUser;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
            result = other;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.FINAL_RECIPIENT);
            result = originalUser;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(userMessage);
    }

    @Test
    public void testValidateOriginalUserOK_originalSender(@Injectable final UserMessage userMessage) {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
        String other = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserOrNullIfAdmin();
            result = originalUser;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
            result = originalUser;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(userMessage);
    }

    @Test
    public void testValidateOriginalUserOK_admin(@Injectable final UserMessage userMessage) {

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserOrNullIfAdmin();
            result = null;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(userMessage);
    }

    @Test(expected = AuthenticationException.class)
    public void validateUserAccess_noAccess(@Injectable final UserMessage userMessage) {
        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
        String other = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserOrNullIfAdmin();
            result = originalUser;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
            result = other;

            userMessageServiceHelper.getProperty(userMessage, MessageConstants.FINAL_RECIPIENT);
            result = other;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorizationWithUnsecureLoginAllowed(userMessage);
    }

}
