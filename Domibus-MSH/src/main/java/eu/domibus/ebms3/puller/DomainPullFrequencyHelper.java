package eu.domibus.ebms3.puller;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Helper that will record errors and success while pulling and adapt the pulling pace per domain/mpc.
 */
public class DomainPullFrequencyHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DomainPullFrequencyHelper.class);

    public static final String DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE = "domibus.pull.request.send.per.job.cycle";

    public static final String DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC_PREFIX = "domibus.pull.request.send.per.job.cycle.per.mpc.";

    public static final String DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME = "domibus.pull.request.frequency.recovery.time";

    public static final String DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT = "domibus.pull.request.frequency.error.count";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    private Map<String, MpcPullFrequency> mpcPullFrequencyMap = new HashMap<>();

    private final Domain domain;

    public DomainPullFrequencyHelper(Domain domain) {
        this.domain = domain;
    }

    public void setMpcNames(Set<String> mpcNames) {
        for (String mpcName : mpcNames) {
            if (mpcPullFrequencyMap.get(mpcName) == null) {
                LOG.debug("Adding mpc:[{}] to frequency helper for domain:[{}]", mpcName, domain.getName());
                addMpcName(mpcName);
            }
        }
    }

    private synchronized void addMpcName(String mpcName) {
        final Integer requestPerJobCyclePerMpc = getNumberOfPullRequestsPerMpc(mpcName);
        final Integer recoveringTimeInSeconds = Integer.valueOf(domibusPropertyProvider.getDomainProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME));
        final Integer numberOfErrorBeforeDecrease = Integer.valueOf(domibusPropertyProvider.getDomainProperty(DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT));
        if (recoveringTimeInSeconds != 0) {
            LOG.debug("Domain pull pace settings->requestPerJobCyclePerMpc:[{}], recoveringTimeInSeconds:[{}], numberOfErrorBeforeDecrease:[{}]", requestPerJobCyclePerMpc, recoveringTimeInSeconds, numberOfErrorBeforeDecrease);
        }
        mpcPullFrequencyMap.put(mpcName, new MpcPullFrequency(requestPerJobCyclePerMpc, recoveringTimeInSeconds, numberOfErrorBeforeDecrease, mpcName));
    }


    private int getNumberOfPullRequestsPerMpc(String mpc) {
        final String defaultNumberOfPullRequestsPerMpc = domibusPropertyProvider.getDomainProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE);
        String numberOfPullRequestsPerMpc = domibusPropertyProvider.getDomainProperty(DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC_PREFIX + mpc);
        if (StringUtils.isEmpty(numberOfPullRequestsPerMpc)) {
            numberOfPullRequestsPerMpc = defaultNumberOfPullRequestsPerMpc;
        }
        LOG.debug("Number of pull requests [{}] per mpc [{}]", numberOfPullRequestsPerMpc, mpc);
        return NumberUtils.toInt(numberOfPullRequestsPerMpc);
    }

    public Integer getPullRequestsNumberForMpc(final String mpc) {
        return mpcPullFrequencyMap.get(mpc).getMaxRequestsPerMpc();
    }

    public int getTotalPullRequestNumberPerJobCycle() {
        int totalNumber = 0;
        for (MpcPullFrequency value : mpcPullFrequencyMap.values()) {
            totalNumber += value.getMaxRequestsPerMpc();
        }
        return totalNumber;
    }

    public void increaseError(final String mpc) {
        if (mpc == null) {
            LOG.warn("Trying to increase error for pull request but mpc is null");
        }
        LOG.debug("Increasing error counter in frequency helper for mpc:[{}]", mpc);
        if (LOG.isTraceEnabled()) {
            traceConfiguration();
        }
        mpcPullFrequencyMap.get(mpc).increaseErrorCounter();
    }

    private void traceConfiguration() {
        LOG.trace("Mpc map is empty:[{}]", mpcPullFrequencyMap.isEmpty());
        for (Map.Entry<String, MpcPullFrequency> mpcDomainPullFrequencyEntry : mpcPullFrequencyMap.entrySet()) {
            LOG.trace("Pull frequency configuration for mpc:[{}] is:[{}]", mpcDomainPullFrequencyEntry.getKey(), mpcDomainPullFrequencyEntry.getValue());
        }
    }

    public void success(final String mpc) {
        LOG.debug("Success pulling in frequency helper for mpc:[{}]", mpc);
        if (LOG.isTraceEnabled()) {
            traceConfiguration();
        }
        mpcPullFrequencyMap.get(mpc).success();
    }
}
