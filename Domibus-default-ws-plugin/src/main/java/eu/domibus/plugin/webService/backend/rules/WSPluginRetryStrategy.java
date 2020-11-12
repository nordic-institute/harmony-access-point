package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.Serializable;
import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public enum WSPluginRetryStrategy {

    CONSTANT("CONSTANT", WSPluginRetryStrategy.ConstantAttemptAlgorithm.ALGORITHM),
    SEND_ONCE("SEND_ONCE", WSPluginRetryStrategy.SendOnceAttemptAlgorithm.ALGORITHM);

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginRetryStrategy.class);

    public static final int DEFAULT_MAX_ATTEMPTS = 60000;

    public static final int MULTIPLIER_MINUTES_TO_MILLIS = 60000;

    private final String name;
    private final WSPluginRetryStrategy.AttemptAlgorithm algorithm;

    WSPluginRetryStrategy(final String name, final WSPluginRetryStrategy.AttemptAlgorithm attemptAlgorithm) {
        this.name = name;
        this.algorithm = attemptAlgorithm;
    }

    public String getName() {
        return this.name;
    }

    public WSPluginRetryStrategy.AttemptAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    public enum ConstantAttemptAlgorithm implements WSPluginRetryStrategy.AttemptAlgorithm {

        ALGORITHM {
            @Override
            public Date compute(final Date received, int maxAttempts, final int timeoutInMinutes) {
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
        };
    }

    public enum SendOnceAttemptAlgorithm implements WSPluginRetryStrategy.AttemptAlgorithm {

        ALGORITHM {
            @Override
            public Date compute(final Date received, final int currentAttempts, final int timeoutInMinutes) {
                LOG.debug("No date to be calculated (SendOnceAttemptAlgorithm). received: [{}] timeoutInMinutes: [{}] received: [{}]", received, timeoutInMinutes, received);
                return null;
            }
        }
    }

    /**
     * NOT FINISHED *
     */
    public interface AttemptAlgorithm extends Serializable {
        Date compute(Date received, int maxAttempts, int timeoutInMinutes);
    }

}
