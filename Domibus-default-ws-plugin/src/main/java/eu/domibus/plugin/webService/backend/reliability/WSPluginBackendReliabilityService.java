package eu.domibus.plugin.webService.backend.reliability;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageStatus;
import eu.domibus.plugin.webService.backend.reliability.strategy.WSPluginRetryStrategy;
import eu.domibus.plugin.webService.backend.reliability.strategy.WSPluginRetryStrategyProvider;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.webService.exception.WSPluginException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static eu.domibus.plugin.webService.backend.reliability.strategy.WSPluginRetryStrategyConstant.MULTIPLIER_MINUTES_TO_MILLIS;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class WSPluginBackendReliabilityService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginBackendReliabilityService.class);

    private final WSBackendMessageLogDao wsBackendMessageLogDao;

    protected final DomibusPropertyExtService domibusPropertyProvider;

    protected final WSPluginRetryStrategyProvider wsPluginRetryStrategyProvider;

    public WSPluginBackendReliabilityService(WSBackendMessageLogDao wsBackendMessageLogDao,
                                             DomibusPropertyExtService domibusPropertyProvider,
                                             WSPluginRetryStrategyProvider wsPluginRetryStrategyProvider) {
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.wsPluginRetryStrategyProvider = wsPluginRetryStrategyProvider;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReliability(WSBackendMessageLogEntity backendMessage, WSPluginDispatchRule rule) {
        backendMessage.setSendAttempts(backendMessage.getSendAttempts() + 1);
        backendMessage.setNextAttempt(backendMessage.getCreationTime());
        backendMessage.setScheduled(false);
        LOG.debug("Backend message [{}] updated for retry", backendMessage);

        if (hasAttemptsLeft(backendMessage, rule.getRetryTimeout())) {
            setWaitingForRetry(backendMessage, rule);
        } else {
            setFailed(backendMessage);
        }
        wsBackendMessageLogDao.update(backendMessage);
    }

    protected void setFailed(WSBackendMessageLogEntity backendMessage) {
        LOG.info("Marking backend notification message id [{}] as failed for domibus id [{}] ",
                backendMessage.getEntityId(),
                backendMessage.getMessageId());
        backendMessage.setFailed(new Date());
        backendMessage.setMessageStatus(WSBackendMessageStatus.SEND_FAILURE);
    }

    /**
     * Check if the message can be send again: there is time and attempts left
     *
     * @param backendMessage the message to check
     * @param retryTimeoutInMinute from {@link WSPluginDispatchRule}
     * @return {@code true} if the message can be send again
     */
    protected boolean hasAttemptsLeft(final WSBackendMessageLogEntity backendMessage, int retryTimeoutInMinute) {

        LOG.debug("Send attempts [{}], max send attempts [{}], scheduled start time [{}], retry timeout [{}]",
                backendMessage.getSendAttempts(),
                backendMessage.getSendAttemptsMax(),
                backendMessage.getCreationTime().getTime(),
                retryTimeoutInMinute);
        // retries start after the first send attempt
        boolean hasMoreAttempts = backendMessage.getSendAttempts() < backendMessage.getSendAttemptsMax();
        long retryTimeout = retryTimeoutInMinute * MULTIPLIER_MINUTES_TO_MILLIS;
        boolean hasMoreTime = (backendMessage.getCreationTime().getTime() + retryTimeout) > System.currentTimeMillis();

        LOG.debug("Verify if has more attempts: [{}] and has more time: [{}]", hasMoreAttempts, hasMoreTime);
        return hasMoreAttempts && hasMoreTime;
    }

    protected void setWaitingForRetry(WSBackendMessageLogEntity backendMessage, WSPluginDispatchRule rule) {
        LOG.info("Marking backend notification message id [{}] as waiting for retry for domibus id [{}] ",
                backendMessage.getEntityId(),
                backendMessage.getMessageId());
        Date nextAttempt = new Date();
        if (backendMessage.getNextAttempt() != null) {
            nextAttempt = new Date(backendMessage.getNextAttempt().getTime());
        }
        WSPluginRetryStrategy strategy = wsPluginRetryStrategyProvider.getStrategy(rule.getRetryStrategy());
        if (strategy == null) {
            throw new WSPluginException(
                    "Strategy not found for rule :[" + backendMessage.getRuleName() + "] " +
                            "for backendMessageLogEntity ID: [" + backendMessage.getEntityId() + "]");
        }
        int retryCount = rule.getRetryCount();
        int retryTimeout = rule.getRetryTimeout();
        Date newNextAttempt = strategy.calculateNextAttempt(nextAttempt, retryCount, retryTimeout);
        LOG.debug("Updating next attempt from [{}] to [{}]", nextAttempt, newNextAttempt);
        backendMessage.setNextAttempt(newNextAttempt);
        backendMessage.setMessageStatus(WSBackendMessageStatus.WAITING_FOR_RETRY);
    }

}


