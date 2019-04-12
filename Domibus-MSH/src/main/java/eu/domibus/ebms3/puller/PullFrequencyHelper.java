package eu.domibus.ebms3.puller;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.model.configuration.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.internal.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Pull frequency helper component in charge of delegating to the adequate domain pull frequency helper components
 */
public class PullFrequencyHelper {

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    private Function<Domain, DomainPullFrequencyHelper> domainPullFrequencyHelperFunction;

    private Map<Domain, DomainPullFrequencyHelper> domainPullFrequencyHelperMap = new HashMap<>();


    public void setResponders(Set<Party> responderParties) {
        getDomainPullFrequencyHelper().setResponders(responderParties);
    }

    public int getTotalPullRequestNumberPerJobCycle() {
        return getDomainPullFrequencyHelper().getTotalPullRequestNumberPerJobCycle();
    }

    public Integer getPullRequestNumberForResponder(final String responderName) {
        return getDomainPullFrequencyHelper().getPullRequestNumberForResponder(responderName);
    }

    public void success(final String partyName) {
        getDomainPullFrequencyHelper().success(partyName);
    }

    public void increaseError(final String partyName) {
        getDomainPullFrequencyHelper().increaseError(partyName);
    }

    private synchronized DomainPullFrequencyHelper configureNewDomain(Domain currentDomain) {
        DomainPullFrequencyHelper domainPullFrequencyHelper = domainPullFrequencyHelperFunction.apply(currentDomain);
        domainPullFrequencyHelperMap.put(currentDomain, domainPullFrequencyHelper);
        return domainPullFrequencyHelper;
    }

    private DomainPullFrequencyHelper getDomainPullFrequencyHelper() {
        Domain currentDomain = domainProvider.getCurrentDomain();
        DomainPullFrequencyHelper domainPullFrequencyHelper = domainPullFrequencyHelperMap.get(currentDomain);
        if (domainPullFrequencyHelper != null) {
            return domainPullFrequencyHelper;
        }
        return configureNewDomain(currentDomain);
    }

}
