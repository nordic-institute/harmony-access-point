package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.ext.delegate.mapper.MessageExtMapper;
import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.MessageMonitorExtException;
import eu.domibus.ext.services.MessageMonitorExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageMonitoringServiceDelegate implements MessageMonitorExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageMonitoringServiceDelegate.class);

    protected UserMessageService userMessageService;

    protected MessageExtMapper messageExtMapper;

    protected MessageAttemptService messageAttemptService;

    protected UserMessageSecurityService userMessageSecurityService;

    public MessageMonitoringServiceDelegate(UserMessageService userMessageService,
                                            MessageExtMapper messageExtMapper,
                                            MessageAttemptService messageAttemptService,
                                            UserMessageSecurityService userMessageSecurityService) {
        this.userMessageService = userMessageService;
        this.messageExtMapper = messageExtMapper;
        this.messageAttemptService = messageAttemptService;
        this.userMessageSecurityService = userMessageSecurityService;
    }

    @Override
    public List<String> getFailedMessages() throws AuthenticationExtException, MessageMonitorExtException {
        String originalUserFromSecurityContext = userMessageSecurityService.getOriginalUserFromSecurityContext();
        return userMessageService.getFailedMessages(originalUserFromSecurityContext);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient) throws AuthenticationExtException, MessageMonitorExtException {
        LOG.debug("Getting failed messages with finalRecipient [{}]", finalRecipient);
        userMessageSecurityService.checkAuthorization(finalRecipient);
        return userMessageService.getFailedMessages(finalRecipient);
    }


    @Override
    public Long getFailedMessageInterval(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        return userMessageService.getFailedMessageElapsedTime(messageId);
    }

    @Override
    public void restoreFailedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        userMessageService.restoreFailedMessage(messageId);
    }

    @Override
    public void sendEnqueuedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException {
        userMessageSecurityService.checkMessageAuthorization(messageId);
        userMessageService.sendEnqueuedMessage(messageId);
    }

    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end) throws AuthenticationExtException, MessageMonitorExtException {
        String originalUserFromSecurityContext = userMessageSecurityService.getOriginalUserFromSecurityContext();
        return userMessageService.restoreFailedMessagesDuringPeriod(begin, end, originalUserFromSecurityContext);
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
}
