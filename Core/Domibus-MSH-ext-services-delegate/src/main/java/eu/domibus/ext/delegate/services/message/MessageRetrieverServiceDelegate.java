package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageStatus;
import eu.domibus.ext.services.MessageRetrieverExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageRetrieverServiceDelegate implements MessageRetrieverExtService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetrieverServiceDelegate.class);

    private final MessageRetriever messageRetriever;

    private final UserMessageSecurityService userMessageSecurityService;

    private final AuthUtils authUtils;

    private final UserMessageService userMessageService;

    public MessageRetrieverServiceDelegate(MessageRetriever messageRetriever,
                                           UserMessageSecurityService userMessageSecurityService,
                                           AuthUtils authUtils, UserMessageService userMessageService) {
        this.messageRetriever = messageRetriever;
        this.userMessageSecurityService = userMessageSecurityService;
        this.authUtils = authUtils;
        this.userMessageService = userMessageService;
    }


    @Override
    public Submission downloadMessage(String messageId) throws MessageNotFoundException {
        checkMessageAuthorization(messageId);

        return messageRetriever.downloadMessage(messageId);
    }

    @Override
    public Submission downloadMessage(Long messageEntityId) throws MessageNotFoundException {
        checkMessageAuthorization(messageEntityId);

        return messageRetriever.downloadMessage(messageEntityId);
    }

    @Override
    public Submission browseMessage(String messageId) throws MessageNotFoundException {
        checkMessageAuthorization(messageId);

        return messageRetriever.browseMessage(messageId);
    }

    @Override
    public Submission browseMessage(String messageId, eu.domibus.common.MSHRole mshRole) throws MessageNotFoundException {
        checkMessageAuthorization(messageId, mshRole);

        return messageRetriever.browseMessage(messageId, mshRole);
    }

    @Override
    public Submission browseMessage(Long messageEntityId) throws MessageNotFoundException {
        checkMessageAuthorization(messageEntityId);

        return messageRetriever.browseMessage(messageEntityId);
    }

    @Override
    public MessageStatus getStatus(String messageId) {
        try {
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, MSHRole.RECEIVING);
        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
            LOG.debug(e.getMessage());
            return eu.domibus.common.MessageStatus.NOT_FOUND;
        }
        return messageRetriever.getStatus(messageId);
    }

    @Override
    public MessageStatus getStatus(String messageId, eu.domibus.common.MSHRole mshRole) {
        MSHRole role = MSHRole.valueOf(mshRole.name());
        try {
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, role);
        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
            LOG.debug(e.getMessage());
            return eu.domibus.common.MessageStatus.NOT_FOUND;
        }
        return messageRetriever.getStatus(messageId, mshRole);
    }

    @Override
    public MessageStatus getStatus(Long messageEntityId) {
        try {
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageEntityId);
        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
            LOG.debug(e.getMessage());
            return eu.domibus.common.MessageStatus.NOT_FOUND;
        }
        return messageRetriever.getStatus(messageEntityId);
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(String messageId) {
        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, MSHRole.RECEIVING);

        return messageRetriever.getErrorsForMessage(messageId);
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(String messageId, eu.domibus.common.MSHRole mshRole) {
        MSHRole role = MSHRole.valueOf(mshRole.name());

        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId, role);

        return messageRetriever.getErrorsForMessage(messageId, mshRole);
    }

    protected void checkMessageAuthorization(Long messageEntityId) {
        checkUnsecure();

        final UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);
        checkMessageAuthorization(userMessage);
    }

    private void checkMessageAuthorization(String messageId, eu.domibus.common.MSHRole mshRole) {
        checkUnsecure();

        MSHRole role = MSHRole.valueOf(mshRole.name());
        final UserMessage userMessage = userMessageService.getByMessageId(messageId, role);
        checkMessageAuthorization(userMessage);
    }

    protected void checkMessageAuthorization(String messageId) {
        checkMessageAuthorization(messageId, eu.domibus.common.MSHRole.RECEIVING);
    }

    private void checkUnsecure() {
        if (authUtils.isUnsecureLoginAllowed()) {
            return;
        }
        authUtils.hasUserOrAdminRole();
    }

    protected void checkMessageAuthorization(UserMessage userMessage) {
        String originalUser = authUtils.getOriginalUserWithUnsecureLoginAllowed();
        String displayUser = originalUser == null ? "super user" : originalUser;
        LOG.debug("Authorized as [{}]", displayUser);

        // Authorization check
        userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
    }
}
