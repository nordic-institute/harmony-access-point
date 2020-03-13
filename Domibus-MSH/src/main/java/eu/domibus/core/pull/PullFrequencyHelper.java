package eu.domibus.core.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
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


    public void setMpcNames(Set<String> mpcNames) {
        getDomainPullFrequencyHelper().setMpcNames(mpcNames);
    }

    public int getTotalPullRequestNumberPerJobCycle() {
        return getDomainPullFrequencyHelper().getTotalPullRequestNumberPerJobCycle();
    }

    public Integer getPullRequestNumberForMpc(final String mpc) {
        return getDomainPullFrequencyHelper().getPullRequestsNumberForMpc(mpc);
    }

    public void success(final String mpc) {
        getDomainPullFrequencyHelper().success(mpc);
    }

    public void increaseError(final String mpc) {
        getDomainPullFrequencyHelper().increaseError(mpc);
    }

    public synchronized void reset() {
        Domain currentDomain = domainProvider.getCurrentDomain();
        domainPullFrequencyHelperMap.remove(currentDomain);
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
