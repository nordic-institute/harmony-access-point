package eu.domibus.plugin.ws.backend.reliability.strategy;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_MINUTE;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginRetryStrategyConstant implements WSPluginRetryStrategy {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginRetryStrategyConstant.class);

    public static final int DEFAULT_MAX_ATTEMPTS = 60000;

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
        final long stopTime = received.getTime() + (timeoutInMinutes * MILLIS_PER_MINUTE) + 5000; // We grant 5 extra seconds to avoid not sending the last attempt
        while (retry <= stopTime) {
            retry += (long) timeoutInMinutes * MILLIS_PER_MINUTE / maxAttempts;
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
