package eu.domibus.plugin.ws.backend.reliability;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.WSBackendMessageStatus;
import eu.domibus.plugin.ws.backend.reliability.strategy.WSPluginRetryStrategy;
import eu.domibus.plugin.ws.backend.reliability.strategy.WSPluginRetryStrategyProvider;
import eu.domibus.plugin.ws.backend.reliability.strategy.WSPluginRetryStrategyType;
import eu.domibus.plugin.ws.backend.rules.WSPluginDispatchRule;
import eu.domibus.plugin.ws.exception.WSPluginException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginBackendReliabilityServiceTest {

    public static final Date ONE_MINUTE_AGO = Date.from(LocalDateTime.now().minusMinutes(1)
            .atZone(ZoneId.systemDefault()).toInstant());
    public static final Date YESTERDAY = Date.from(LocalDateTime.now().minusDays(1)
            .atZone(ZoneId.systemDefault()).toInstant());
    public static final Date A_DATE = Date.from(LocalDateTime.of(2020, 12, 31, 23, 59)
            .atZone(ZoneId.systemDefault()).toInstant());
    public static final Date NEW_DATE = Date.from(LocalDateTime.of(2030, 12, 31, 23, 59)
            .atZone(ZoneId.systemDefault()).toInstant());
    public static final long ID = 12L;
    public static final String RULE_NAME = "ruleName";
    public static final int RETRY_COUNT = 5;
    private static final int RETRY_TIMOUT = 10;
    public static final int ATTEMPTS = 2;
    @Tested
    private WSPluginBackendReliabilityService reliabilityService;

    @Injectable
    private WSBackendMessageLogDao wsBackendMessageLogDao;

    @Injectable
    protected DomibusPropertyExtService domibusPropertyProvider;

    @Injectable
    protected WSPluginRetryStrategyProvider strategyProvider;

    @Test
    public void updateNextAttempt(@Mocked WSBackendMessageLogEntity backendMessage,
                                  @Mocked WSPluginDispatchRule rule,
                                  @Mocked WSPluginRetryStrategy retryStrategy) {
        new Expectations() {{
            backendMessage.getEntityId();
            result = 1L;

            backendMessage.getMessageId();
            result = "messageId";

            backendMessage.getNextAttempt();
            result = A_DATE;
            times = ATTEMPTS;

            rule.getRetryStrategy();
            result = WSPluginRetryStrategyType.CONSTANT;
            times = 1;

            strategyProvider.getStrategy(WSPluginRetryStrategyType.CONSTANT);
            result = retryStrategy;
            times = 1;

            rule.getRetryCount();
            times = 1;
            result = RETRY_COUNT;

            rule.getRetryTimeout();
            times = 1;
            result = RETRY_TIMOUT;

            retryStrategy.calculateNextAttempt(A_DATE, RETRY_COUNT, RETRY_TIMOUT);
            result = NEW_DATE;
            times = 1;
        }};

        reliabilityService.setWaitingForRetry(backendMessage, rule);

        new FullVerifications() {{
            backendMessage.setNextAttempt(NEW_DATE);
            times = 1;

            backendMessage.setBackendMessageStatus(WSBackendMessageStatus.WAITING_FOR_RETRY);
            times = 1;
        }};
    }

    @Test(expected = WSPluginException.class)
    public void updateNextAttempt_noStrategy(
            @Mocked WSBackendMessageLogEntity backendMessage,
            @Mocked WSPluginDispatchRule rule,
            @Mocked WSPluginRetryStrategy retryStrategy) {
        new Expectations() {{
            backendMessage.getNextAttempt();
            result = A_DATE;
            times = ATTEMPTS;

            rule.getRetryStrategy();
            result = WSPluginRetryStrategyType.CONSTANT;
            times = 1;

            strategyProvider.getStrategy(WSPluginRetryStrategyType.CONSTANT);
            result = null;
            times = 1;
        }};

        reliabilityService.setWaitingForRetry(backendMessage, rule);

        new FullVerifications() {
        };
    }

    @Test
    public void hasAttemptsLeft_true(@Mocked WSBackendMessageLogEntity backendMessage) {
        new Expectations() {{
            backendMessage.getSendAttempts();
            result = ATTEMPTS;
            backendMessage.getSendAttemptsMax();
            result = 10;
            backendMessage.getCreationTime();
            result = ONE_MINUTE_AGO;
        }};
        Assert.assertTrue(reliabilityService.hasAttemptsLeft(backendMessage, RETRY_TIMOUT));
    }

    @Test
    public void hasAttemptsLeft_false(@Mocked WSBackendMessageLogEntity backendMessage) {
        new Expectations() {{
            backendMessage.getSendAttempts();
            result = ATTEMPTS;
            backendMessage.getSendAttemptsMax();
            result = 10;
            backendMessage.getCreationTime();
            result = YESTERDAY;
        }};
        Assert.assertFalse(reliabilityService.hasAttemptsLeft(backendMessage, RETRY_TIMOUT));
    }

    @Test
    public void hasAttemptsLeft_outOfAttempt(@Mocked WSBackendMessageLogEntity backendMessage) {
        new Expectations() {{
            backendMessage.getSendAttempts();
            result = 15;
            backendMessage.getSendAttemptsMax();
            result = 10;
            backendMessage.getCreationTime();
            result = ONE_MINUTE_AGO;
        }};
        Assert.assertFalse(reliabilityService.hasAttemptsLeft(backendMessage, RETRY_TIMOUT));
    }

    @Test
    public void setFailed() {

        WSBackendMessageLogEntity backendMessageLogEntity = new WSBackendMessageLogEntity();
        reliabilityService.setFailed(backendMessageLogEntity);

        new FullVerifications(){};
        Assert.assertNotNull(backendMessageLogEntity.getFailed());
        Assert.assertEquals(WSBackendMessageStatus.SEND_FAILURE, backendMessageLogEntity.getBackendMessageStatus());
    }

    @Test
    public void handleReliability_hasAttemptsLeft(@Mocked WSBackendMessageLogEntity backendMessage,
                                                  @Mocked WSPluginDispatchRule rule) {

        new Expectations(reliabilityService) {{
            backendMessage.getSendAttempts();
            result = ATTEMPTS;

            backendMessage.getCreationTime();
            result = ONE_MINUTE_AGO;

            rule.getRetryTimeout();
            result = RETRY_TIMOUT;

            reliabilityService.hasAttemptsLeft(backendMessage, RETRY_TIMOUT);
            result = true;
            times = 1;

            reliabilityService.setWaitingForRetry(backendMessage, rule);
            times = 1;
        }};

        reliabilityService.handleReliability(backendMessage, rule);

        new Verifications() {{
            backendMessage.setSendAttempts(ATTEMPTS + 1);
            times = 1;
            backendMessage.setNextAttempt(ONE_MINUTE_AGO);
            times = 1;
            backendMessage.setScheduled(false);
            times = 1;

            wsBackendMessageLogDao.update(backendMessage);
            times = 1;
        }};
    }

    @Test
    public void handleReliability_noAttemptsLeft(@Mocked WSBackendMessageLogEntity backendMessage,
                                                  @Mocked WSPluginDispatchRule rule) {

        new Expectations(reliabilityService) {{
            backendMessage.getSendAttempts();
            result = ATTEMPTS;

            backendMessage.getCreationTime();
            result = ONE_MINUTE_AGO;

            rule.getRetryTimeout();
            result = RETRY_TIMOUT;

            reliabilityService.hasAttemptsLeft(backendMessage, RETRY_TIMOUT);
            result = false;
            times = 1;

            reliabilityService.setFailed(backendMessage);
            times = 1;
        }};

        reliabilityService.handleReliability(backendMessage, rule);

        new Verifications() {{
            backendMessage.setSendAttempts(ATTEMPTS + 1);
            times = 1;
            backendMessage.setNextAttempt(ONE_MINUTE_AGO);
            times = 1;
            backendMessage.setScheduled(false);
            times = 1;

            wsBackendMessageLogDao.update(backendMessage);
            times = 1;
        }};
    }
}