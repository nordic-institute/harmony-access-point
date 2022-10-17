package eu.domibus.core.message;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_RESEND_ALL_BATCH_COUNT_LIMIT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_RESEND_ALL_MAX_COUNT;

/**
 * This service class is responsible for the restore of failed messages.
 *
 * @author Soumya
 * @since 4.2.2
 */

@Service
public class UserMessageDefaultRestoreService implements UserMessageRestoreService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultRestoreService.class);

    private static final int MAX_RESEND_MESSAGE_COUNT = 100;
    private static final String RESEND_SELECTED = "selected";

    private MessageExchangeService messageExchangeService;

    private BackendNotificationService backendNotificationService;

    private UserMessageLogDao userMessageLogDao;

    private PModeProvider pModeProvider;

    private PullMessageService pullMessageService;

    private PModeService pModeService;

    private PModeServiceHelper pModeServiceHelper;

    private UserMessageDefaultService userMessageService;

    private UserMessageDao userMessageDao;

    private AuditService auditService;

    private DomibusPropertyProvider domibusPropertyProvider;

    private DomainTaskExecutor domainTaskExecutor;

    public UserMessageDefaultRestoreService(MessageExchangeService messageExchangeService, BackendNotificationService backendNotificationService,
                                            UserMessageLogDao userMessageLogDao, PModeProvider pModeProvider, PullMessageService pullMessageService,
                                            PModeService pModeService, PModeServiceHelper pModeServiceHelper, UserMessageDefaultService userMessageService,
                                            UserMessageDao userMessageDao, AuditService auditService, DomibusPropertyProvider domibusPropertyProvider,
                                            DomainTaskExecutor domainTaskExecutor) {
        this.messageExchangeService = messageExchangeService;
        this.backendNotificationService = backendNotificationService;
        this.userMessageLogDao = userMessageLogDao;
        this.pModeProvider = pModeProvider;
        this.pullMessageService = pullMessageService;
        this.pModeService = pModeService;
        this.pModeServiceHelper = pModeServiceHelper;
        this.userMessageService = userMessageService;
        this.userMessageDao = userMessageDao;
        this.auditService = auditService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainTaskExecutor = domainTaskExecutor;
    }

    @Transactional
    @Override
    public void resendFailedOrSendEnqueuedMessage(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId);
        }
        if (MessageStatus.SEND_ENQUEUED == userMessageLog.getMessageStatus()) {
            userMessageService.sendEnqueuedMessage(messageId);
        } else {
            userMessageService.restoreFailedMessage(messageId);
        }

        auditService.addMessageResentAudit(messageId);
    }

    @Transactional
    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Long failedStartDate, Long failedEndDate, String finalRecipient, String originalUser) {
        final List<String> failedMessages = userMessageLogDao.findFailedMessages(finalRecipient, originalUser, failedStartDate, failedEndDate);
        if (CollectionUtils.isEmpty(failedMessages)) {
            return null;
        }
        LOG.debug("Found failed messages [{}] using start ID_PK date-hour [{}], end ID_PK date-hour [{}] and final recipient [{}]", failedMessages, failedStartDate, failedEndDate, finalRecipient);

        final List<String> restoredMessages = new ArrayList<>();
        for (String messageId : failedMessages) {
            try {
                userMessageService.restoreFailedMessage(messageId);
                restoredMessages.add(messageId);
            } catch (Exception e) {
                LOG.error("Failed to restore message [" + messageId + "]", e);
            }
        }

        LOG.debug("Restored messages [{}] using start ID_PK date-hour [{}], end ID_PK date-hour [{}] and final recipient [{}]", restoredMessages, failedStartDate, failedEndDate, finalRecipient);

        return restoredMessages;
    }

    @Transactional
    @Override
    public List<String> restoreAllOrSelectedFailedMessages(final List<String> messageIds, String resendAllOrSelected) {
        if (CollectionUtils.isEmpty(messageIds)) {
            return null;
        }
        final List<String> restoredMessages = new ArrayList<>();

        if (resendAllOrSelected.equals(RESEND_SELECTED)) {
            validationForRestoreSelected(messageIds);
            restoreBatchMessages(restoredMessages, messageIds);
        } else {
            validationForRestoreAll(messageIds);

            final List<String> totalMessageIds = new ArrayList<>();
            totalMessageIds.addAll(messageIds);
            int resendBatchLimit = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_RESEND_ALL_BATCH_COUNT_LIMIT);

            for (int i = 0; i < messageIds.size(); i += resendBatchLimit) {
                List<String> batchMessageIds = totalMessageIds.stream().limit(resendBatchLimit).collect(Collectors.toList());
                restoreBatchMessages(restoredMessages, batchMessageIds);
                totalMessageIds.removeAll(batchMessageIds);
            }
        }
        LOG.debug("Restored messages [{}] and final recipient [{}]", restoredMessages);

        return restoredMessages;
    }

    protected void validationForRestoreSelected(List<String> messageIds) {
        if (messageIds.size() > MAX_RESEND_MESSAGE_COUNT) {
            LOG.warn("Couldn't resend the selected messages. The selected message counts exceeds maximum number of messages to resend at once: " + MAX_RESEND_MESSAGE_COUNT);
            throw new MessagingException("The resend message counts exceeds maximum number of messages to resend at once: " + MAX_RESEND_MESSAGE_COUNT, null);
        }
    }

    @Transactional
    public void restoreBatchMessages(List<String> restoredMessages, List<String> batchMessageIds) {
        for (String messageId : batchMessageIds) {
            LOG.debug("Message Id's selected to resend as batch [{}]", messageId);
            try {
                domainTaskExecutor.submit(() -> userMessageService.restoreFailedMessage(messageId));
                restoredMessages.add(messageId);
            } catch (Exception e) {
                LOG.error("Failed to restore message [" + messageId + "]", e);
                throw new MessagingException("Failed to restore message: " + messageId, null);
            }
        }
    }

    protected void validationForRestoreAll(List<String> messageIds) {
        int maxResendCount = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_RESEND_ALL_MAX_COUNT);

        if (maxResendCount < messageIds.size()) {
            LOG.warn("Couldn't resend the messages. The resend message counts exceeds maximum resend count limit: " + maxResendCount);
            throw new MessagingException("The resend message counts exceeds maximum resend count limit: " + maxResendCount, null);
        }
    }
}
