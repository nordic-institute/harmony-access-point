package eu.domibus.core.message.pull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class MpcPullFrequency {

    private static final Logger LOG = LoggerFactory.getLogger(MpcPullFrequency.class);

    private Integer maxRequestsPerMpc;

    private Integer recoveringTimeInSeconds;

    private Integer numberOfErrorToTriggerFrequencyDecrease;

    private AtomicInteger adaptableRequestPerJobCycle = new AtomicInteger(1);

    private AtomicInteger errorCounter = new AtomicInteger(0);

    private AtomicLong executionTime = new AtomicLong(0);

    private AtomicBoolean fullCapacity = new AtomicBoolean(Boolean.FALSE);

    private AtomicBoolean lowCapacity = new AtomicBoolean(Boolean.FALSE);

    private AtomicInteger increment = new AtomicInteger(0);

    private String mpc;

    public MpcPullFrequency(
            final Integer maxRequestsPerMpc,
            final Integer recoveringTimeInSeconds,
            final Integer numberOfErrorToTriggerFrequencyDecrease, String mpc) {
        this.maxRequestsPerMpc = maxRequestsPerMpc;
        this.recoveringTimeInSeconds = recoveringTimeInSeconds;
        this.numberOfErrorToTriggerFrequencyDecrease = numberOfErrorToTriggerFrequencyDecrease;
        this.mpc = mpc;
    }

    private synchronized void error() {
        if (recoveringTimeInSeconds != 0) {
            adaptableRequestPerJobCycle.set(1);
            increment.set(0);
            fullCapacity.set(false);
            lowCapacity.set(true);
        }
    }

    public void success() {
        boolean changed = lowCapacity.compareAndSet(true, false);
        if(changed) {
            LOG.trace("Low capacity switched from true to false, error counter is reset.");
            errorCounter.set(0);
        }
    }

    /**
     * Not need
     */
    public void increaseErrorCounter() {
        if (recoveringTimeInSeconds != 0 && !lowCapacity.get()) {
            final int numberOfError = errorCounter.incrementAndGet();
            if (numberOfError >= numberOfErrorToTriggerFrequencyDecrease) {
                LOG.trace("Number of pull errors:[{}] >= number of error to trigger frequency decrease:[{}] for mpc:[{}]-> reset frequency", numberOfError, numberOfErrorToTriggerFrequencyDecrease, mpc);
                error();
            }
        }
    }

    public Integer getMaxRequestsPerMpc() {
        LOG.trace("recoveringTimeInSeconds:[{}], fullCapacity:[{}], low capacity:[{}], maxRequestsPerMpc:[{}]", recoveringTimeInSeconds, fullCapacity, lowCapacity, 1);
        if (lowCapacity.get()) {
            LOG.trace("get max pull request for Low capacity activated for mpc:[{}], pull request pace=1", mpc);
            return 1;
        }
        if (recoveringTimeInSeconds == 0) {
            fullCapacity.compareAndSet(false,true);
            LOG.trace("Recovering time is 0 therefore set full capacity:[{}] for mpc:[{}]", fullCapacity, mpc);
        }
        if(fullCapacity.get()){
            LOG.trace("Max request per job for mpc:[{}] is:[{}] ", mpc, maxRequestsPerMpc);
            return maxRequestsPerMpc;
        }

        final long previousTime = executionTime.get();
        final long updatedTime = executionTime.updateAndGet(operand -> {
                    if (operand == 0 || (System.currentTimeMillis() - operand > 1000)) {
                        return System.currentTimeMillis();
                    }
                    return operand;
                }
        );
        if (previousTime != updatedTime) {
            final int newValue = this.increment.addAndGet(1);
            final double ratio = newValue * (maxRequestsPerMpc / Double.valueOf(recoveringTimeInSeconds));
            final double i = maxRequestsPerMpc / ratio;
            final Double temporaryPace = maxRequestsPerMpc / i;
            adaptableRequestPerJobCycle.set(temporaryPace.intValue() + 1);
            if (adaptableRequestPerJobCycle.get() >= maxRequestsPerMpc) {
                fullCapacity.set(true);
            }
            int newPace = adaptableRequestPerJobCycle.get();
            LOG.trace("New pull frequency pace calculate:[{}] at :[{}] for mpc:[{}]", newPace,previousTime, mpc);
        }
        return adaptableRequestPerJobCycle.get();
    }

    @Override
    public String toString() {
        return "MpcPullFrequency {" +
                "maxRequestsPerMpc=" + maxRequestsPerMpc +
                ", recoveringTimeInSeconds=" + recoveringTimeInSeconds +
                ", numberOfErrorToTriggerFrequencyDecrease=" + numberOfErrorToTriggerFrequencyDecrease +
                ", adaptableRequestPerJobCycle=" + adaptableRequestPerJobCycle +
                ", errorCounter=" + errorCounter +
                ", executionTime=" + executionTime +
                ", fullCapacity=" + fullCapacity +
                ", lowCapacity=" + lowCapacity +
                ", increment=" + increment +
                '}';
    }
}
