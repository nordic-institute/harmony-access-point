package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
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

import java.util.List;

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
        final UserMessage userMessage = userMessageService.getByMessageId(messageId);
        checkMessageAuthorization(userMessage);

        return messageRetriever.downloadMessage(messageId);
    }

    @Override
    public Submission downloadMessage(Long messageEntityId) throws MessageNotFoundException {
        final UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);
        checkMessageAuthorization(userMessage);

        return messageRetriever.downloadMessage(messageEntityId);
    }

    @Override
    public Submission browseMessage(String messageId) throws MessageNotFoundException {
        LOG.info("Browsing message with id [{}]", messageId);
        UserMessage userMessage = userMessageService.getByMessageId(messageId);
        checkMessageAuthorization(userMessage);

        return messageRetriever.browseMessage(messageId);
    }

    @Override
    public Submission browseMessage(Long messageEntityId) throws MessageNotFoundException {
        LOG.info("Browsing message with entity id [{}]", messageEntityId);
        UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);
        checkMessageAuthorization(userMessage);

        return messageRetriever.browseMessage(messageEntityId);
    }

    @Override
    public MessageStatus getStatus(String messageId) {
        try {
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
            LOG.debug(e.getMessage());
            return eu.domibus.common.MessageStatus.NOT_FOUND;
        }
        return messageRetriever.getStatus(messageId);
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
        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);

        return messageRetriever.getErrorsForMessage(messageId);
    }

    protected void checkMessageAuthorization(UserMessage userMessage) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }



        String originalUser = authUtils.getOriginalUserWithUnsecureLoginAllowed();
        String displayUser = originalUser == null ? "super user" : originalUser;
        LOG.debug("Authorized as [{}]", displayUser);

        // Authorization check
        userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
    }
}
