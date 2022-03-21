package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageSecurityDefaultService implements UserMessageSecurityService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageSecurityDefaultService.class);

    protected AuthUtils authUtils;
    protected UserMessageService userMessageService;
    protected UserMessageServiceHelper userMessageServiceHelper;

    public UserMessageSecurityDefaultService(AuthUtils authUtils, UserMessageService userMessageService, UserMessageServiceHelper userMessageServiceHelper) {
        this.authUtils = authUtils;
        this.userMessageService = userMessageService;
        this.userMessageServiceHelper = userMessageServiceHelper;
    }

    @Override
    public void checkMessageAuthorizationWithUnsecureLoginAllowed(UserMessage userMessage) throws AuthenticationException {
        try {
            validateUserAccess(userMessage);
        } catch (AccessDeniedException e) {
            throw new AuthenticationException("You are not allowed to access message [" + userMessage.getMessageId() + "]. Reason: [" + e.getMessage() + "]", e);
        }
    }

    @Override
    public void checkAuthorization(String finalRecipient) throws AuthenticationException {
        /* unsecured login allowed */
        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return;
        }

        final String originalUserFromSecurityContext = authUtils.getOriginalUser();
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
        return authUtils.getOriginalUserWithUnsecureLoginAllowed();
    }

    /**
     * @param userMessage with set of {@link eu.domibus.api.model.MessageProperty}
     * @throws AccessDeniedException if the authOriginalUser is not ORIGINAL_SENDER or FINAL_RECIPIENT of the {@link UserMessage}
     */
    public void validateUserAccessWithUnsecureLoginAllowed(UserMessage userMessage) throws AccessDeniedException {
        /* unsecured login allowed */
        if (authUtils.isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return;
        }
        validateUserAccess(userMessage);
    }

    public void validateUserAccess(UserMessage userMessage) {
        String authOriginalUser = authUtils.getOriginalUser();
        List<String> propertyNames = new ArrayList<>();
        propertyNames.add(MessageConstants.ORIGINAL_SENDER);
        propertyNames.add(MessageConstants.FINAL_RECIPIENT);

        if (StringUtils.isBlank(authOriginalUser)) {
            LOG.trace("OriginalUser is [{}] is admin", authOriginalUser);
            return;
        }

        LOG.trace("OriginalUser is [{}] not admin", authOriginalUser);

        /* check the message belongs to the authenticated user */
        boolean found = false;
        for (String propertyName : propertyNames) {
            String originalUser = userMessageServiceHelper.getProperty(userMessage, propertyName);
            if (StringUtils.equalsIgnoreCase(originalUser, authOriginalUser)) {
                found = true;
                break;
            }
        }
        if (!found) {
            LOG.debug("Could not validate originalUser for [{}]", authOriginalUser);
            throw new AccessDeniedException("You are not allowed to handle this message [" + userMessage.getMessageId() + "]. You are authorized as [" + authOriginalUser + "]");
        }
        LOG.trace("Could validate originalUser for [{}]", authOriginalUser);
    }

    public void validateUserAccessWithUnsecureLoginAllowed(UserMessage userMessage, String authOriginalUser, String propertyName) {
        if (StringUtils.isBlank(authOriginalUser)) {
            LOG.trace("OriginalUser is [{}] admin", authOriginalUser);
            return;
        }
        LOG.trace("OriginalUser is [{}] not admin", authOriginalUser);
        /* check the message belongs to the authenticated user */
        String originalUser = userMessageServiceHelper.getProperty(userMessage, propertyName);
        if (!StringUtils.equalsIgnoreCase(originalUser, authOriginalUser)) {
            LOG.debug("User [{}] is trying to submit/access a message having as final recipient: [{}]", authOriginalUser, originalUser);
            throw new AccessDeniedException("You are not allowed to handle this message. You are authorized as [" + authOriginalUser + "]");
        }
    }

    public void checkMessageAuthorizationWithUnsecureLoginAllowed(final Long messageEntityId) {
        UserMessage userMessage = userMessageService.findByEntityId(messageEntityId);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageEntityId);
        }
        validateUserAccessWithUnsecureLoginAllowed(userMessage);
    }

    public void checkMessageAuthorizationWithUnsecureLoginAllowed(String messageId) {
        UserMessage userMessage = userMessageService.findByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageId);
        }
        validateUserAccessWithUnsecureLoginAllowed(userMessage);
    }

    public void checkMessageAuthorization(String messageId) {
        UserMessage userMessage = userMessageService.findByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageId);
        }
        try {
            validateUserAccess(userMessage);
        } catch (AccessDeniedException e) {
            throw new AuthenticationException("You are not allowed to access message [" + userMessage.getMessageId() + "]. Reason: [" + e.getMessage() + "]", e);
        }
    }

}
