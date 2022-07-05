package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.plugin.handler.MessageRetrieverImpl;
import eu.domibus.ext.services.MessageRetrieverExtService;
import eu.domibus.plugin.handler.MessageRetriever;
import mockit.Injectable;
import mockit.Tested;

public class MessageRetrieverServiceDelegateTest  {
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

//    @Test
//    public void testDownloadMessageAuthUserNok(@Injectable UserMessage userMessage,
//                                               @Injectable final UserMessageLog messageLog) {
//
//        String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
//        new Expectations(messageRetriever) {{
//            authUtils.isUnsecureLoginAllowed();
//            result = false;
//
//            authUtils.getOriginalUserWithUnsecureLoginAllowed();
//            result = originalUser;
//
//            userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
//            result = new AccessDeniedException("You are not allowed to handle this message");
//        }};
//
//        try {
//            messageRetriever.checkMessageAuthorization(userMessage);
//            Assert.fail("It should throw AccessDeniedException");
//        } catch (AccessDeniedException adEx) {
//            LOG.debug("Expected :", adEx);
//            assertTrue(adEx.getMessage().contains("You are not allowed to handle this message"));
//        }
//
//        new Verifications() {{
//            authUtils.hasUserOrAdminRole();
//            authUtils.getOriginalUserWithUnsecureLoginAllowed();
//        }};
//
//    }

    //    @Test
//    public void testGetStatus() {
//        // Given
//        new Expectations() {{
//
//
//            userMessageLogService.getMessageStatus(MESS_ID);
//            result = MessageStatus.ACKNOWLEDGED;
//        }};
//
//        // When
//        final eu.domibus.common.MessageStatus status = messageRetriever.getStatus(MESS_ID);
//
//        Assert.assertEquals(eu.domibus.common.MessageStatus.ACKNOWLEDGED, status);
//
//        new Verifications() {{
//            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(MESS_ID);
//            times = 1;
//        }};
//
//    }

    //    @Test
//    public void testGetStatusAccessDenied() {
//        // Given
//        new Expectations() {{
//            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(MESS_ID);
//            result = new AccessDeniedException("");
//        }};
//
//        // When
//        eu.domibus.common.MessageStatus status = null;
//        try {
//            status = messageRetriever.getStatus(MESS_ID);
//            Assert.fail("It should throw AccessDeniedException");
//        } catch (AccessDeniedException ex) {
//            // ok
//        }
//
//        Assert.assertNull(status);
//
//    }
}
