package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.*;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.resend.MessageResendEntity;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.scheduler.DomibusQuartzStarter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.domibus.logging.DomibusMessageCode.BUS_MESSAGE_RESTORED_FAILED;
import static eu.domibus.logging.DomibusMessageCode.BUS_MESSAGE_RESTORED_SUCCESS;

/**
 * This service class is responsible for the restore of failed messages.
 *
 * @author Soumya
 * @since 4.2.2
 */

@Service
public class UserMessageDefaultRestoreService implements UserMessageRestoreService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultRestoreService.class);

    protected static int MAX_RESEND_MESSAGE_COUNT = 10;

    private final MessageExchangeService messageExchangeService;

    private final UserMessageLogDefaultService userMessageLogDefaultService;

    private final PModeProvider pModeProvider;

    private final PullMessageService pullMessageService;

    private final PModeService pModeService;

    private final PModeServiceHelper pModeServiceHelper;

    private final UserMessageDefaultService userMessageService;

    private final UserMessageDao userMessageDao;

    private final AuditService auditService;

    private final UserMessageRestoreDao userMessageRestoreDao;

    private final DomibusQuartzStarter domibusQuartzStarter;

    private final PlatformTransactionManager transactionManager;

    public UserMessageDefaultRestoreService(MessageExchangeService messageExchangeService, UserMessageLogDefaultService userMessageLogDao,
                                            PModeProvider pModeProvider, PullMessageService pullMessageService,
                                            PModeService pModeService, PModeServiceHelper pModeServiceHelper, UserMessageDefaultService userMessageService,
                                            UserMessageDao userMessageDao, AuditService auditService, UserMessageRestoreDao userMessageRestoreDao,
                                            DomibusQuartzStarter domibusQuartzStarter, PlatformTransactionManager transactionManager) {
        this.messageExchangeService = messageExchangeService;
        this.userMessageLogDefaultService = userMessageLogDao;
        this.pModeProvider = pModeProvider;
        this.pullMessageService = pullMessageService;
        this.pModeService = pModeService;
        this.pModeServiceHelper = pModeServiceHelper;
        this.userMessageService = userMessageService;
        this.userMessageDao = userMessageDao;
        this.auditService = auditService;
        this.userMessageRestoreDao = userMessageRestoreDao;
        this.domibusQuartzStarter = domibusQuartzStarter;
        this.transactionManager = transactionManager;
    }


    @Transactional
    @Override
    public void restoreFailedMessage(String messageId) {

        final UserMessageLog userMessageLog = userMessageService.getFailedMessage(messageId);

        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ENTITY_ID, String.valueOf(userMessageLog.getEntityId()));
        try {
            LOG.info("Restoring message [{}]-[{}]", messageId, MSHRole.SENDING);
            if (MessageStatus.DELETED == userMessageLog.getMessageStatus() || MessageStatus.ACKNOWLEDGED == userMessageLog.getMessageStatus()) {
                throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not restore message [" + messageId + "]. Message status is [" + userMessageLog.getMessageStatus() + "]");
            }

            UserMessage userMessage = userMessageDao.findByEntityId(userMessageLog.getEntityId());

            final MessageStatusEntity newMessageStatus = messageExchangeService.retrieveMessageRestoreStatus(messageId, userMessage.getMshRole().getRole());
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, newMessageStatus.getMessageStatus());

            final Date currentDate = new Date();
            userMessageLog.setRestored(currentDate);
            userMessageLog.setFailed(null);
            userMessageLog.setNextAttempt(currentDate);

            Integer newMaxAttempts = computeNewMaxAttempts(userMessageLog);
            LOG.debug("Increasing the max attempts for message [{}] from [{}] to [{}]", messageId, userMessageLog.getSendAttemptsMax(), newMaxAttempts);
            userMessageLog.setSendAttemptsMax(newMaxAttempts);

            userMessageLogDefaultService.update(userMessageLog);

            if (MessageStatus.READY_TO_PULL != newMessageStatus.getMessageStatus()) {
                userMessageService.scheduleSending(userMessage, userMessageLog);
                LOG.businessInfo(BUS_MESSAGE_RESTORED_SUCCESS);
            } else {
                LOG.businessError(BUS_MESSAGE_RESTORED_FAILED);
                try {
                    MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true);
                    String pModeKey = userMessageExchangeConfiguration.getPmodeKey();
                    LOG.debug("[restoreFailedMessage]:Message:[{}] add lock", userMessage.getMessageId());
                    pullMessageService.addPullMessageLock(userMessage, userMessageLog);
                } catch (EbMS3Exception ebms3Ex) {
                    LOG.error("Error restoring user message to ready to pull[" + userMessage.getMessageId() + "]", ebms3Ex);
                }
            }
        } finally {
            LOG.removeMDC(DomibusLogger.MDC_MESSAGE_ID);
            LOG.removeMDC(DomibusLogger.MDC_MESSAGE_ENTITY_ID);
        }
    }

    protected Integer getMaxAttemptsConfiguration(final Long messageEntityId) {
        final LegConfiguration legConfiguration = pModeService.getLegConfiguration(messageEntityId);
        Integer result = 1;
        if (legConfiguration == null) {
            LOG.warn("Could not get the leg configuration for message with entity id [{}]. Using the default maxAttempts configuration [{}]", messageEntityId, result);
        } else {
            result = pModeServiceHelper.getMaxAttempts(legConfiguration);
        }
        return result;
    }

    protected Integer computeNewMaxAttempts(final UserMessageLog userMessageLog) {
        Integer maxAttemptsConfiguration = getMaxAttemptsConfiguration(userMessageLog.getEntityId());
        // always increase maxAttempts (even when not reached by sendAttempts)
        return userMessageLog.getSendAttemptsMax() + maxAttemptsConfiguration + 1; // max retries plus initial reattempt
    }

    @Transactional
    @Override
    public void resendFailedOrSendEnqueuedMessage(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDefaultService.findByMessageId(messageId, MSHRole.SENDING);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId);
        }
        if (MessageStatus.SEND_ENQUEUED == userMessageLog.getMessageStatus()) {
            userMessageService.sendEnqueuedMessage(messageId, userMessageLog.getEntityId());
        } else {
            restoreFailedMessage(messageId);
        }

        auditService.addMessageResentAudit(messageId);
    }

    @Transactional
    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Long failedStartDate, Long failedEndDate, String finalRecipient, String originalUser) {

        final List<String> failedMessages = userMessageLogDefaultService.findFailedMessages(finalRecipient, originalUser, failedStartDate, failedEndDate);
        if (CollectionUtils.isEmpty(failedMessages)) {
            return null;
        }
        LOG.debug("Found failed messages [{}] using start ID_PK date-hour [{}], end ID_PK date-hour [{}] and final recipient [{}]", failedMessages, failedStartDate, failedEndDate, finalRecipient);

        final List<String> restoredMessages = new ArrayList<>();
        for (String messageId : failedMessages) {
            try {
                restoreFailedMessage(messageId);
                restoredMessages.add(messageId);
            } catch (Exception e) {
                LOG.error("Failed to restore message [" + messageId + "]", e);
            }
        }

        LOG.debug("Restored messages [{}] using start ID_PK date-hour [{}], end ID_PK date-hour [{}] and final recipient [{}]", restoredMessages, failedStartDate, failedEndDate, finalRecipient);

        return restoredMessages;
    }

    @Override
    public void restoreFailedMessages(final List<String> messageIds) throws SchedulerException {
        if (CollectionUtils.isEmpty(messageIds)) {
            return;
        }
        if (messageIds.size() > MAX_RESEND_MESSAGE_COUNT) {
            LOG.info("Triggering the messageResendJob to restore [{}] failed messages", messageIds.size());
            triggerMessageResendJob(messageIds);
            return;
        }
        LOG.debug("Restoring the failed messages without messageResendJob.");
        restoreMessages(messageIds);
    }

    protected void triggerMessageResendJob(List<String> messageIds) throws SchedulerException {
        int failedCount = 0;
        for (String messageId : messageIds) {
            try {
                MessageResendEntity messageResendEntity = new MessageResendEntity();
                messageResendEntity.setMessageId(messageId);
                userMessageRestoreDao.create(messageResendEntity);
            } catch (Exception ex) {
                LOG.warn("Could not enqueue message [{}] for resending", messageId, ex);
                failedCount++;
            }
        }
        domibusQuartzStarter.triggerMessageResendJob();
        if (failedCount > 0) {
            LOG.info("Failed messages enqueued for restoring. [{}]/[{}] messages could not be enqueued (possibly already enqueued).", failedCount, messageIds.size());
        } else {
            LOG.info("All [{}] failed messages were enqueued for restoring successfully.", messageIds.size());
        }
    }

    @Override
    public List<String> findAllMessagesToRestore() {
        return userMessageRestoreDao.findAllMessagesToRestore();
    }


    public void restoreMessages(List<String> messageIds) {
        for (String messageId : messageIds) {
            LOG.debug("Message Id's selected to restore [{}]", messageId);
            try {
                new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
                    @SuppressWarnings("squid:S2229")
                    //we can ignore the Sonar check here because the transaction is created by TransactionTemplate
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        restoreFailedMessage(messageId);
                    }
                });
            } catch (Exception e) {
                LOG.error("Failed to restore message [" + messageId + "]", e);
            }
        }
    }

    @Override
    public void findAndRestoreFailedMessages() {
        List<String> messageIds = findAllMessagesToRestore();
        if (CollectionUtils.isEmpty(messageIds)) {
            return;
        }

        LOG.info("Found [{}] failed messages to restore", messageIds.size());
        int failedCount = 0;
        for (String messageId : messageIds) {
            LOG.debug("Found message to restore. Starting the restoring process of message with messageId [{}]", messageId);
            try {
                new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
                    @SuppressWarnings("squid:S2229")
                    //we can ignore the Sonar check here because the transaction is created by TransactionTemplate
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        restoreFailedMessage(messageId);
                        userMessageRestoreDao.delete(messageId);
                    }
                });
            } catch (Exception e) {
                LOG.error("Failed to restore message [" + messageId + "]", e);
                failedCount++;
            }
        }
        LOG.info("Restoring process of failed messages completed. [{}]/[{}] messages could not be restored", failedCount, messageIds.size());
    }
}