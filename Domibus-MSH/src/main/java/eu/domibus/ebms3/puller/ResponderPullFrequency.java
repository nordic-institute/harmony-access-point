package eu.domibus.ebms3.puller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ResponderPullFrequency {

    private static final Logger LOG = LoggerFactory.getLogger(ResponderPullFrequency.class);

    private Integer maxRequestPerJobCycle;

    private Integer recoveringTimeInSeconds;

    private Integer numberOfErrorToTriggerFrequencyDecrease;

    private AtomicInteger adaptableRequestPerJobCycle = new AtomicInteger(1);

    private AtomicInteger errorCounter = new AtomicInteger(0);

    private AtomicLong executionTime = new AtomicLong(0);

    private AtomicBoolean fullCapacity = new AtomicBoolean(Boolean.FALSE);

    private AtomicBoolean lowCapacity = new AtomicBoolean(Boolean.FALSE);

    private AtomicInteger increment = new AtomicInteger(0);

    private String responderName;


    public ResponderPullFrequency(
            final Integer maxRequestPerJobCycle,
            final Integer recoveringTimeInSeconds,
            final Integer numberOfErrorToTriggerFrequencyDecrease, String responderName) {
        this.maxRequestPerJobCycle = maxRequestPerJobCycle;
        this.recoveringTimeInSeconds = recoveringTimeInSeconds;
        this.numberOfErrorToTriggerFrequencyDecrease = numberOfErrorToTriggerFrequencyDecrease;
        this.responderName = responderName;
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
            LOG.trace("Low capacity switched from tru to false, error counter is reset.");
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
                LOG.trace("Number of pull errors:[{}] >= number of error to trigger frequency decrease:[{}] for responder:[{}]-> reset frequency", numberOfError, numberOfErrorToTriggerFrequencyDecrease,responderName);
                error();
            }
        }
    }

    public Integer getMaxRequestPerJobCycle() {
        LOG.trace("recoveringTimeInSeconds:[{}], fullCapacity:[{}], low capacity:[{}], maxRequestPerJobCycle:[{}]", recoveringTimeInSeconds, fullCapacity, lowCapacity, 1);
        if (lowCapacity.get()) {
            LOG.trace("get max pull request for Low capacity activated for responderName:[{}], pull request pace=1", responderName);
            return 1;
        }
        if (recoveringTimeInSeconds == 0) {
            fullCapacity.compareAndSet(false,true);
            LOG.trace("Recovering time is 0 therefore set full capacity:[{}] for responder:[{}]", fullCapacity, responderName);
        }
        if(fullCapacity.get()){
            LOG.trace("Max request per job for responder:[{}] is:[{}] ", responderName,maxRequestPerJobCycle);
            return maxRequestPerJobCycle;
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
            final double ratio = newValue * (maxRequestPerJobCycle / Double.valueOf(recoveringTimeInSeconds));
            final double i = maxRequestPerJobCycle / ratio;
            final Double temporaryPace = maxRequestPerJobCycle / i;
            adaptableRequestPerJobCycle.set(temporaryPace.intValue() + 1);
            if (adaptableRequestPerJobCycle.get() >= maxRequestPerJobCycle) {
                fullCapacity.set(true);
            }
            int newPace = adaptableRequestPerJobCycle.get();
            LOG.trace("New pull frequency pace calculate:[{}] at :[{}] for responder party:[{}]", newPace,previousTime, responderName);
        }
        return adaptableRequestPerJobCycle.get();
    }

    @Override
    public String toString() {
        return "DomainPullFrequency{" +
                "maxRequestPerJobCycle=" + maxRequestPerJobCycle +
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
