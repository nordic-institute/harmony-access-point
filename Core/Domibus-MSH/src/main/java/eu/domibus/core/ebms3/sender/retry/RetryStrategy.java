
package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.Serializable;
import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.MILLIS_PER_MINUTE;

/**
 * @author Christian Koch, Stefan Mueller
 */
public enum RetryStrategy {

    CONSTANT("CONSTANT", RetryStrategy.ConstantAttemptAlgorithm.ALGORITHM), SEND_ONCE("SEND_ONCE", RetryStrategy.SendOnceAttemptAlgorithm.ALGORITHM),
    PROGRESSIVE("PROGRESSIVE", ProgressiveAttemptAlgorithm.ALGORITHM);

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetryStrategy.class);
    private final String name;
    private final RetryStrategy.AttemptAlgorithm algorithm;

    RetryStrategy(final String name, final RetryStrategy.AttemptAlgorithm attemptAlgorithm) {
        this.name = name;
        this.algorithm = attemptAlgorithm;
    }

    public String getName() {
        return this.name;
    }

    public RetryStrategy.AttemptAlgorithm getAlgorithm() {
        return this.algorithm;
    }


    public enum ConstantAttemptAlgorithm implements RetryStrategy.AttemptAlgorithm {

        ALGORITHM {
            @Override
            public Date compute(final Date received, int maxAttempts, final int timeoutInMinutes, final int crtInterval, final long delayInMillis) {
                if (maxAttempts <= 0 || timeoutInMinutes <= 0 || received == null) {
                    return null;
                }
                if(maxAttempts > MILLIS_PER_MINUTE) {
                    maxAttempts = (int)MILLIS_PER_MINUTE;
                }
                final long now = System.currentTimeMillis();
                long retry = received.getTime();
                final long stopTime = received.getTime() + ( (long)timeoutInMinutes * MILLIS_PER_MINUTE ) + delayInMillis; // We grant some extra time (configured in properties as milliseconds) to avoid not sending the last attempt
                while (retry <= stopTime) {
                    retry += (long)timeoutInMinutes * MILLIS_PER_MINUTE / maxAttempts;
                    if (retry > now && retry < stopTime) {
                        return new Date(retry);
                    }
                }
                return null;
            }

        }
    }

    public enum SendOnceAttemptAlgorithm implements RetryStrategy.AttemptAlgorithm {

        ALGORITHM {
            @Override
            public Date compute(final Date received, final int currentAttempts, final int timeoutInMinutes, final int crtInterval, final long delayInMillis) {

                return null;
            }
        }
    }

    public enum ProgressiveAttemptAlgorithm implements RetryStrategy.AttemptAlgorithm {

        ALGORITHM {

            @Override
            public Date compute(Date received, int maxAttempts, int timeoutInMinutes, int crtInterval, long delayInMillis) {
                if(maxAttempts <= 0 || timeoutInMinutes <= 0 || received == null) {
                    return null;
                }
                if(maxAttempts > MILLIS_PER_MINUTE) {
                    maxAttempts = (int)MILLIS_PER_MINUTE;
                }
                final long now = System.currentTimeMillis();
                long retry = received.getTime();
                final long stopTime = received.getTime() + ( (long)timeoutInMinutes * MILLIS_PER_MINUTE ) + delayInMillis; // We grant some extra time (configured in properties as milliseconds) to avoid not sending the last attempt
                while (retry <= stopTime) {
                    LOG.debug("Computing the time for the next retry...");
                    retry = retry + (long) crtInterval * MILLIS_PER_MINUTE;
                    if (retry > now && retry < stopTime) {
                        LOG.debug("Next attempt time: " + retry);
                        return new Date(retry);
                    } else {
                        LOG.debug("Next retry would exceed the stopTime. Next attempt calculation skipped. Exiting...");
                    }
                }
                return null;
            }
        }
    }

    public interface AttemptAlgorithm extends Serializable {
        /**
         * Calculates the time for the next attempt
         * @param received
         * @param maxAttempts
         * @param timeoutInMinutes - the total timeout for retrials since the initial failed attempt
         * @param crtInterval - the current interval (in minutes) in a progressive intervals list since the initial attempt.
         *                    This parameter is not used by the other strategies
         * @param delayInMillis
         * @return the calculated time for the next attempt
         */
        Date compute(Date received, int maxAttempts, int timeoutInMinutes, int crtInterval, long delayInMillis);
    }
}
