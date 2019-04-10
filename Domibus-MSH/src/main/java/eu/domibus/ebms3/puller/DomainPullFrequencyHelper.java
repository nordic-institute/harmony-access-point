package eu.domibus.ebms3.puller;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Party;
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
 * Helper that will record errors and success while pulling and adapt the pulling pace per domain/repsonder party.
 */
public class DomainPullFrequencyHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DomainPullFrequencyHelper.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    private Map<String, ResponderPullFrequencyConfiguration> responderPartyPullFrequencyMap = new HashMap<>();

    private final Domain domain;

    public DomainPullFrequencyHelper(Domain domain) {
        this.domain = domain;
    }

    public void setResponders(Set<Party> responderParties) {
        for (Party responderParty : responderParties) {
            if (responderPartyPullFrequencyMap.get(responderParty.getName()) == null) {
                LOG.debug("Adding party:[{}] to frequency helper for domain:[{}]", responderParty.getName(), domain.getName());
                addParty(responderParty);
            }
        }
    }

    private synchronized void addParty(Party responderParty) {
        final Integer requestPerJobCycle = Integer.valueOf(domibusPropertyProvider.getDomainProperty("domibus.pull.request.send.per.job.cycle"));
        final Integer recoveringTimeInSeconds = Integer.valueOf(domibusPropertyProvider.getDomainProperty("domibus.pull.request.frequency.recovery.time"));
        final Integer numberOfErrorBeforeDecrease = Integer.valueOf(domibusPropertyProvider.getDomainProperty("domibus.pull.request.frequency.error.count"));
        if (recoveringTimeInSeconds != 0) {
            LOG.info("Domain pull pace settings->requestPerJobCycle:[{}], recoveringTimeInSeconds:[{}], numberOfErrorBeforeDecrease:[{}]", requestPerJobCycle, recoveringTimeInSeconds, numberOfErrorBeforeDecrease);
        }
        responderPartyPullFrequencyMap.put(responderParty.getName(), new ResponderPullFrequencyConfiguration(requestPerJobCycle, recoveringTimeInSeconds, numberOfErrorBeforeDecrease));
    }

    public Integer getPullRequestNumberForResponder(final String responderName) {
        return responderPartyPullFrequencyMap.get(responderName).getMaxRequestPerJobCycle();
    }

    public int getTotalPullRequestNumberPerJobCycle() {
        int totalNumber = 0;
        for (ResponderPullFrequencyConfiguration value : responderPartyPullFrequencyMap.values()) {
            totalNumber += value.getMaxRequestPerJobCycle();
        }
        return totalNumber;
    }

    public void increaseError(final String partyName) {
        if (partyName == null) {
            LOG.warn("Trying to increase error for pull request but responder party name is null");
        }
        LOG.debug("Increasing error counter in frequency helper for party:[{}]", partyName);
        if (LOG.isTraceEnabled()) {
            traceConfiguration();
        }
        responderPartyPullFrequencyMap.get(partyName).increaseErrorCounter();
    }

    private void traceConfiguration() {
        LOG.trace("Party map is empty:[{}]", responderPartyPullFrequencyMap.isEmpty());
        for (Map.Entry<String, ResponderPullFrequencyConfiguration> partyDomainPullFrequencyEntry : responderPartyPullFrequencyMap.entrySet()) {
            LOG.trace("Pull frequency configuration for responder:[{}] is:[{}]", partyDomainPullFrequencyEntry.getKey(), partyDomainPullFrequencyEntry.getValue());
        }
    }

    public void success(final String partyName) {
        LOG.debug("Success pulling in frequency helper for party:[{}]", partyName);
        if (LOG.isTraceEnabled()) {
            traceConfiguration();
        }
        responderPartyPullFrequencyMap.get(partyName).success();
    }
}
