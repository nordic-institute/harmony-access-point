
package eu.domibus.core.ebms3.sender.retry;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Christian Koch, Stefan Mueller
 */
public enum RetryStrategy {

    CONSTANT("CONSTANT", RetryStrategy.ConstantAttemptAlgorithm.ALGORITHM), SEND_ONCE("SEND_ONCE", RetryStrategy.SendOnceAttemptAlgorithm.ALGORITHM);

    private final String name;
    private final RetryStrategy.AttemptAlgorithm algorithm;
    private static final int MULTIPLIER_MINUTES_TO_SECONDS = 60000;

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
            public Date compute(final Date received, int maxAttempts, final int timeoutInMinutes, final long delayInMillis) {
                if(maxAttempts < 0 || timeoutInMinutes < 0 || received == null) {
                    return null;
                }
                if(maxAttempts > MULTIPLIER_MINUTES_TO_SECONDS) {
                    maxAttempts = MULTIPLIER_MINUTES_TO_SECONDS;
                }
                final long now = System.currentTimeMillis();
                long retry = received.getTime();
                final long stopTime = received.getTime() + ( (long)timeoutInMinutes * MULTIPLIER_MINUTES_TO_SECONDS ) + delayInMillis; // We grant some extra time (configured in properties as milliseconds) to avoid not sending the last attempt
                while (retry <= (stopTime)) {
                    retry += (long)timeoutInMinutes * MULTIPLIER_MINUTES_TO_SECONDS / maxAttempts;
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
            public Date compute(final Date received, final int currentAttempts, final int timeoutInMinutes, final long delayInMillis) {

                return null;
            }
        }
    }

    public interface AttemptAlgorithm extends Serializable {
        Date compute(Date received, int maxAttempts, int timeoutInMinutes, long delayInMillis);
    }
}
