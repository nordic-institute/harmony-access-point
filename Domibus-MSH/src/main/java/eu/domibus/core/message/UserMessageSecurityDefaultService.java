package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageSecurityDefaultService implements UserMessageSecurityService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageSecurityDefaultService.class);

    protected AuthUtils authUtils;
    protected UserMessageService userMessageService;

    public UserMessageSecurityDefaultService(AuthUtils authUtils,
                                             UserMessageService userMessageService) {
        this.authUtils = authUtils;
        this.userMessageService = userMessageService;
    }

    @Override
    public void checkMessageAuthorization(String messageId) throws AuthenticationException {
        /* unsecured login allowed */
        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return;
        }

        final String finalRecipient = userMessageService.getFinalRecipient(messageId);
        if (StringUtils.isEmpty(finalRecipient)) {
            throw new AuthenticationException("Couldn't get the finalRecipient for message with ID [" + messageId + "]");
        }
        checkAuthorization(finalRecipient);
    }

    @Override
    public void checkAuthorization(String finalRecipient) throws AuthenticationException {
        /* unsecured login allowed */
        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return;
        }

        final String originalUserFromSecurityContext = authUtils.getOriginalUserFromSecurityContext();
        if (StringUtils.isEmpty(originalUserFromSecurityContext)) {
            LOG.debug("finalRecipient from the security context is empty, user has permission to access finalRecipient [{}]", finalRecipient);
            return;
        }

        if (StringUtils.equals(finalRecipient, originalUserFromSecurityContext)) {
            LOG.debug("The provided finalRecipient [{}] is the same as the user's finalRecipient", finalRecipient);
        } else {
            LOG.securityInfo(DomibusMessageCode.SEC_UNAUTHORIZED_MESSAGE_ACCESS, originalUserFromSecurityContext, finalRecipient);
            throw new AuthenticationException("You are not allowed to access messages for finalRecipient [" + finalRecipient + "]. You are authorized as [" + originalUserFromSecurityContext + "]");
        }
    }

    @Override
    public String getOriginalUserFromSecurityContext() throws AuthenticationException {
        return authUtils.getOriginalUserFromSecurityContext();
    }

    @Override
    public boolean isAdminMultiAware() {
        return authUtils.isAdminMultiAware();
    }

}
