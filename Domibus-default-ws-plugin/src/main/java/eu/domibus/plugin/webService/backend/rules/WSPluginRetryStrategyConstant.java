package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public class WSPluginRetryStrategyConstant implements WSPluginRetryStrategy {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginRetryStrategyConstant.class);

    public static final int DEFAULT_MAX_ATTEMPTS = 60000;

    public static final int MULTIPLIER_MINUTES_TO_MILLIS = 60000;

    @Override
    public Date calculateNextAttempt(Date received, int maxAttempts, int timeoutInMinutes) {
        LOG.debug("Compute next date. maxAttempts: [{}] timeoutInMinutes: [{}] received: [{}]", maxAttempts, timeoutInMinutes, received);

        if (maxAttempts < 0 || timeoutInMinutes < 0 || received == null) {
            LOG.debug("No date to be calculated.");
            return null;
        }
        if (maxAttempts > DEFAULT_MAX_ATTEMPTS) {
            maxAttempts = DEFAULT_MAX_ATTEMPTS;
        }
        final long now = System.currentTimeMillis();
        long retry = received.getTime();
        final long stopTime = received.getTime() + ((long) timeoutInMinutes * MULTIPLIER_MINUTES_TO_MILLIS) + 5000; // We grant 5 extra seconds to avoid not sending the last attempt
        while (retry <= stopTime) {
            retry += (long) timeoutInMinutes * MULTIPLIER_MINUTES_TO_MILLIS / maxAttempts;
            if (retry > now && retry < stopTime) {
                return new Date(retry);
            }
        }
        LOG.debug("No date to be calculated.");
        return null;
    }

    @Override
    public boolean canHandle(WSPluginRetryStrategyType strategyType) {
        return strategyType == WSPluginRetryStrategyType.CONSTANT;
    }

}