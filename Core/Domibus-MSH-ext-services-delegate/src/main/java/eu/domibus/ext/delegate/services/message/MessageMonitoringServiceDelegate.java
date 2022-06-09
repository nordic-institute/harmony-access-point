package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.ext.delegate.mapper.MessageExtMapper;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.MessageMonitorExtException;
import eu.domibus.ext.services.MessageMonitorExtService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageMonitoringServiceDelegate implements MessageMonitorExtService {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringServiceDelegate.class);

    protected UserMessageService userMessageService;

    protected MessageExtMapper messageExtMapper;

    protected MessageAttemptService messageAttemptService;

    protected UserMessageSecurityService userMessageSecurityService;

    private AuthUtils authUtils;
    protected UserMessageRestoreService restoreService;

    public MessageMonitoringServiceDelegate(UserMessageService userMessageService,
                                            MessageExtMapper messageExtMapper,
                                            MessageAttemptService messageAttemptService,
                                            UserMessageSecurityService userMessageSecurityService,
                                            AuthUtils authUtils,
                                            UserMessageRestoreService restoreService) {
        this.userMessageService = userMessageService;
        this.messageExtMapper = messageExtMapper;
        this.messageAttemptService = messageAttemptService;
        this.userMessageSecurityService = userMessageSecurityService;
        this.authUtils = authUtils;
        this.restoreService = restoreService;
    }

    @Override
    public List<String> getFailedMessages() throws AuthenticationExtException, MessageMonitorExtException {
        return getFailedMessages(null);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient) throws AuthenticationExtException, MessageMonitorExtException {
        LOG.debug("Getting failed messages with finalRecipient [{}]", finalRecipient);
        String originalUserFromSecurityContext = getUser();
        return userMessageService.getFailedMessages(finalRecipient, originalUserFromSecurityContext);
    }

    @Override
    public Long getFailedMessageInterval(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        return userMessageService.getFailedMessageElapsedTime(messageId);
    }

    @Override
    public void restoreFailedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        restoreService.restoreFailedMessage(messageId);
    }

    @Override
    public void sendEnqueuedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        userMessageService.sendEnqueuedMessage(messageId);
    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Long begin, Long end) throws AuthenticationExtException, MessageMonitorExtException {
        String originalUserFromSecurityContext = getUser();
        return restoreService.restoreFailedMessagesDuringPeriod(begin, end, null, originalUserFromSecurityContext);
    }

    @Override
    public void deleteFailedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        userMessageService.deleteFailedMessage(messageId);
    }

    @Override
    public List<MessageAttemptDTO> getAttemptsHistory(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        final List<MessageAttempt> attemptsHistory = messageAttemptService.getAttemptsHistory(messageId);
        return messageExtMapper.messageAttemptToMessageAttemptDTO(attemptsHistory);
    }

    @Override
    public void deleteMessageNotInFinalStatus(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        userMessageService.deleteMessageNotInFinalStatus(messageId);
    }

    @Override
    public List<String> deleteMessagesDuringPeriod(Long begin, Long end) throws AuthenticationExtException, MessageMonitorExtException {
        String originalUserFromSecurityContext = getUser();
        return userMessageService.deleteMessagesDuringPeriod(begin, end, originalUserFromSecurityContext);
    }

    private String getUser() {
        String originalUserFromSecurityContext = authUtils.getOriginalUser();
        if(StringUtils.isBlank(originalUserFromSecurityContext) && !authUtils.isAdminMultiAware()) {
            throw new AuthenticationExtException(DomibusErrorCode.DOM_002, "User is not admin");
        }
        return originalUserFromSecurityContext;
    }
}
