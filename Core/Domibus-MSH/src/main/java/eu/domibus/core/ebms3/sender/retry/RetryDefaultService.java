package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.pull.MessagingLock;
import eu.domibus.core.message.pull.MessagingLockDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MSH_RETRY_TIMEOUT_DELAY;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_RECEIPT_TIMEOUT;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_MINUTE;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @author Catalin Enache
 */
@Service
public class RetryDefaultService implements RetryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetryDefaultService.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    UserMessageDefaultService userMessageService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private MessagingLockDao messagingLockDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    private DateUtil dateUtil;

    /**
     * Tries to enqueue a message to be retried.
     *
     * @param messageEntityId The id_pk to be enqueued for retrial
     */
    @Override
    @Transactional
    public void enqueueMessage(long messageEntityId) {
        try {
            doEnqueueMessage(messageEntityId);
        } catch (RuntimeException e) {
            LOG.warn("Could not enqueue message with entityId [{}]", messageEntityId, e);
        }
    }

    /**
     * Tries to enqueue a message to be retried.
     *
     * @param messageEntityId The message entity id to be enqueued for retrial
     */
    protected void doEnqueueMessage(long messageEntityId) {
        LOG.trace("Enqueueing message for retrial with entityId [{}]", messageEntityId);

        final UserMessage userMessage = userMessageDao.findByEntityId(messageEntityId);
        if (userMessage.isSourceMessage()) {
            LOG.debug("Source message [{}] not scheduled for retry.", userMessage.getMessageId());
            return;
        }
        LOG.trace("Enqueueing message for retrial [{}]", userMessage.getMessageId());

        final LegConfiguration legConfiguration = updateRetryLoggingService.getLegConfiguration(userMessage);

        boolean invalidConfig = updateRetryLoggingService.failIfInvalidConfig(userMessage, legConfiguration);
        if (invalidConfig) {
            LOG.warn("Message was not enqueued: invalid LegConfiguration for message [{}]", userMessage.getMessageId());
            return;
        }

        boolean setAsExpired = updateRetryLoggingService.failIfExpired(userMessage, legConfiguration);
        if (setAsExpired) {
            LOG.debug("Message [{}] was marked as expired", userMessage.getMessageId());
            return;
        }
        final UserMessageLog userMessageLog = userMessageLogDao.findByEntityIdSafely(messageEntityId);
        userMessageService.scheduleSending(userMessage, userMessageLog);
    }

    @Override
    public List<Long> getMessagesNotAlreadyScheduled() {
        List<Long> result = new ArrayList<>();

        int maxRetryTimeout = pModeProvider.getMaxRetryTimeout();
        int retryTimeoutDelay = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MSH_RETRY_TIMEOUT_DELAY);
        LOG.trace("maxRetryTimeout [{}], retryTimeoutDelay [{}]", maxRetryTimeout, retryTimeoutDelay);

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        int timeOutMin = maxRetryTimeout + retryTimeoutDelay;
        long minEntityId = dateUtil.getMinEntityId(MINUTES.toSeconds(timeOutMin));
        long maxEntityId = dateUtil.getMaxEntityId(0);

        LOG.trace("minEntityId [{}], maxEntityId [{}]", minEntityId, maxEntityId);
        final List<Long> messageEntityIdsToSend = userMessageLogDao.findRetryMessages(minEntityId, maxEntityId);
        if (messageEntityIdsToSend.isEmpty()) {
            LOG.trace("No message found to be resend");
            return result;
        }
        LOG.trace("Found messages to be send [{}]", messageEntityIdsToSend);

        // START - This part should NOT be propagated to 5.2 (TSID is making the filter works correctly)
        for (Long entityId : messageEntityIdsToSend) {
            UserMessageLog byEntityId = userMessageLogDao.findByEntityId(entityId);

            long timeout = timeOutMin * MILLIS_PER_MINUTE;
            if ((byEntityId.getCreationTime().getTime() + timeout) > now.toInstant().toEpochMilli()) {
                LOG.debug("Add EntityId [{}] creationTime [{}] now [{}] timeout [{} m]", entityId, byEntityId.getCreationTime().toInstant().atOffset(ZoneOffset.UTC), now, timeOutMin);
                result.add(entityId);
            } else {
                LOG.debug("Ignore EntityId [{}] creationTime [{}] now [{}] timeout [{} m]", entityId, byEntityId.getCreationTime().toInstant().atOffset(ZoneOffset.UTC), now, timeOutMin);
            }
        }
        // END - This part should NOT be propagated to 5.2

        return result;
    }

    /**
     * Method called by job to reset waiting_for_receipt messages into ready to pull.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Timer(clazz = RetryDefaultService.class, value = "pull_messages_reset")
    @Counter(clazz = RetryDefaultService.class, value = "pull_messages_reset")
    public void resetWaitingForReceiptPullMessages() {
        final int receiptTimeoutInMinutes = domibusPropertyProvider.getIntegerProperty(DOMIBUS_PULL_RECEIPT_TIMEOUT);
        final Date olderThan = dateUtil.getDateMinutesAgo(receiptTimeoutInMinutes);
        final List<MessagingLock> messagesToReset = messagingLockDao.findWaitingForReceipt(olderThan);
        if (messagesToReset.isEmpty()) {
            LOG.trace("No messages to reset in waiting for receipt state (older than [{}] minutes)", receiptTimeoutInMinutes);
            return;
        }
        LOG.info("Resetting [{}] messages in waiting for receipt state (older than [{}] minutes)", messagesToReset.size(), receiptTimeoutInMinutes);
        for (MessagingLock messagingLock : messagesToReset) {
            pullMessageService.resetMessageInWaitingForReceiptState(messagingLock.getMessageId());
        }
    }


    /**
     * Method call by job to to expire messages that could not be delivered in the configured time range..
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Timer(clazz = RetryDefaultService.class, value = "pull_messages_expire")
    @Counter(clazz = RetryDefaultService.class, value = "pull_messages_expire")
    public void bulkExpirePullMessages() {
        final List<MessagingLock> expiredMessages = messagingLockDao.findStaledMessages();
        LOG.trace("Delete expired pull message");
        for (MessagingLock staledMessage : expiredMessages) {
            pullMessageService.expireMessage(staledMessage.getMessageId(), MSHRole.SENDING);
        }
    }

    /**
     * Method call by job to delete messages marked as failed.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Timer(clazz = RetryDefaultService.class, value = "pull_messages_delete")
    @Counter(clazz = RetryDefaultService.class, value = "pull_messages_delete")
    public void bulkDeletePullMessages() {
        final List<MessagingLock> deletedLocks = messagingLockDao.findDeletedMessages();
        LOG.trace("Delete unnecessary locks");
        for (MessagingLock deletedLock : deletedLocks) {
            pullMessageService.deleteInNewTransaction(deletedLock.getMessageId());
        }
    }
}
