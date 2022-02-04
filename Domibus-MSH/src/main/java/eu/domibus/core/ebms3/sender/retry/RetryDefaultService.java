package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.pull.MessagingLock;
import eu.domibus.core.message.pull.MessagingLockDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.pmode.provider.LegConfigurationPerMpc;
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
import java.util.List;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.*;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MSH_RETRY_TIMEOUT_DELAY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

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
    PModeProvider pModeProvider;

    @Autowired
    UpdateRetryLoggingService updateRetryLoggingService;


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
        if(userMessage.isSourceMessage()) {
            LOG.debug("Source message [{}] not scheduled for retry.", userMessage.getMessageId());
            return;
        }
        LOG.trace("Enqueueing message for retrial [{}]", userMessage.getMessageId());

        final LegConfiguration legConfiguration  = updateRetryLoggingService.getLegConfiguration(userMessage);

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

        int maxRetryTimeout = getMaxRetryTimeout();
        int retryTimeoutDelay = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MSH_RETRY_TIMEOUT_DELAY);

        LOG.trace("maxRetryTimeout [{}] retryTimeoutDelay [{}]", maxRetryTimeout, retryTimeoutDelay);
        long minEntityId = createMinEntityId(maxRetryTimeout + retryTimeoutDelay);
        long maxEntityId = getCurrentTimeMaxEntityId();


        LOG.trace("minEntityId [{}] maxEntityId [{}]", minEntityId, maxEntityId);
        final List<Long> messageEntityIdsToSend = userMessageLogDao.findRetryMessages(minEntityId, maxEntityId);
        if (messageEntityIdsToSend.isEmpty()) {
            LOG.trace("No message found to be resend");
            return result;
        }
        LOG.trace("Found messages to be send [{}]", messageEntityIdsToSend);

        return messageEntityIdsToSend;
    }

    /**
     * Method call by job to reset waiting_for_receipt messages into ready to pull.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetWaitingForReceiptPullMessages() {
        final List<MessagingLock> messagesToReset = messagingLockDao.findWaitingForReceipt();
        for (MessagingLock messagingLock : messagesToReset) {
            pullMessageService.resetMessageInWaitingForReceiptState(messagingLock.getMessageId());
        }
    }


    /**
     * Method call by job to to expire messages that could not be delivered in the configured time range..
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkExpirePullMessages() {
        final List<MessagingLock> expiredMessages = messagingLockDao.findStaledMessages();
        LOG.trace("Delete expired pull message");
        for (MessagingLock staledMessage : expiredMessages) {
            pullMessageService.expireMessage(staledMessage.getMessageId());
        }
    }

    /**
     * Method call by job to to delete messages marked as failed.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkDeletePullMessages() {
        final List<MessagingLock> deletedLocks = messagingLockDao.findDeletedMessages();
        LOG.trace("Delete unecessary locks");
        for (MessagingLock deletedLock : deletedLocks) {
            pullMessageService.deleteInNewTransaction(deletedLock.getMessageId());
        }
    }

    protected int getMaxRetryTimeout() {
        final LegConfigurationPerMpc legConfigurationPerMpc = pModeProvider.getAllLegConfigurations();
        int maxRetry = -1;
        List<LegConfiguration> legConfigurations = new ArrayList<>();
        legConfigurationPerMpc.entrySet().stream().forEach(r -> legConfigurations.addAll(r.getValue()));

        for(LegConfiguration legConfiguration : legConfigurations) {
            int retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout();
            if(maxRetry < retryTimeout) {
                maxRetry = retryTimeout;
            }
        }

        LOG.debug("Got max retryTimeout [{}]", maxRetry);
        return maxRetry;
    }

    protected long createMaxEntityId(int delay) {
        return Long.parseLong(ZonedDateTime
                .now(ZoneOffset.UTC)
                .minusMinutes(delay)
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX);
    }

    protected long createMinEntityId(int delay) {
        return Long.parseLong(ZonedDateTime
                .now(ZoneOffset.UTC)
                .minusMinutes(delay)
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MIN);
    }

    protected long getCurrentTimeMaxEntityId() {
        return createMaxEntityId(0);
    }

}
