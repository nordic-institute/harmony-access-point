package eu.domibus.core.message;

import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.logging.DomibusLogger;
import mockit.*;
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
    AuthUtils authUtils;


    @Test(expected = AuthenticationException.class)
    public void testCheckMessageAuthorizationWithNonExistingMessage() throws Exception {
        final String messageId = "1";
        new Expectations() {{
            userMessageService.getFinalRecipient(messageId);
            result = null;
        }};

        userMessageSecurityDefaultService.checkMessageAuthorization(messageId);
    }

    @Test
    public void testCheckMessageAuthorizationWithExistingMessage() throws Exception {
        final String messageId = "1";
        final String finalRecipient = "C4";
        new Expectations(userMessageSecurityDefaultService) {{
            userMessageService.getFinalRecipient(messageId);
            result = finalRecipient;

            userMessageSecurityDefaultService.checkAuthorization(finalRecipient);
        }};

        userMessageSecurityDefaultService.checkMessageAuthorization(messageId);
    }

    @Test
    public void testCheckAuthorizationWithAdminRole(final @Capturing DomibusLogger log) throws Exception {
        final String finalRecipient = "C4";
        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = null;
        }};

        userMessageSecurityDefaultService.checkAuthorization(finalRecipient);

        new Verifications() {{
            log.debug("finalRecipient from the security context is empty, user has permission to access finalRecipient [{}]", finalRecipient);
            times = 1;
        }};
    }

    @Test(expected = AuthenticationException.class)
    public void testCheckSecurityWhenOriginalUserFromSecurityContextIsDifferent() throws Exception {
        final String finalRecipient = "C4";
        final String originalUserFromSecurityContext = "differentRecipient";

        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = originalUserFromSecurityContext;
        }};

        userMessageSecurityDefaultService.checkAuthorization(finalRecipient);
    }

    @Test
    public void testCheckSecurityWhenOriginalUserFromSecurityContextIsSame() throws Exception {
        final String finalRecipient = "C4";
        final String originalUserFromSecurityContext = "C4";

        new Expectations() {{
            authUtils.getOriginalUserFromSecurityContext();
            result = originalUserFromSecurityContext;
        }};

        userMessageSecurityDefaultService.checkAuthorization(finalRecipient);
    }
}
