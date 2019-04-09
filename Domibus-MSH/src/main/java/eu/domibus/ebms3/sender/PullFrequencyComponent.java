package eu.domibus.ebms3.sender;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class PullFrequencyComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PullFrequencyComponent.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    private Map<Domain, DomainPullFrequency> domainDomainPullFrequencyMap = new HashMap();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            final Integer requestPerJobCycle = Integer.valueOf(domibusPropertyProvider.getDomainProperty("domibus.pull.request.send.per.job.cycle"));
            final Integer recoveringTimeInSeconds = Integer.valueOf(domibusPropertyProvider.getDomainProperty("domibus.pull.request.frequency.recovery.time"));
            final Integer numberOfErrorBeforeDecrease = Integer.valueOf(domibusPropertyProvider.getDomainProperty("domibus.pull.request.frequency.error.count"));
            if (recoveringTimeInSeconds != 0) {
                LOG.info("Domain pull pace settings->requestPerJobCycle:[{}], recoveringTimeInSeconds:[{}], numberOfErrorBeforeDecrease:[{}]", requestPerJobCycle, recoveringTimeInSeconds, numberOfErrorBeforeDecrease);
            }
            domainDomainPullFrequencyMap.put(domain, new DomainPullFrequency(requestPerJobCycle, recoveringTimeInSeconds, numberOfErrorBeforeDecrease));
        }
    }

    public Integer getPullRequestNumberPerJobCycle() {
        return domainDomainPullFrequencyMap.get(domainContextProvider.getCurrentDomainSafely()).getMaxRequestPerJobCycle();
    }

    public void increaseError() {
        domainDomainPullFrequencyMap.get(domainContextProvider.getCurrentDomainSafely()).increaseErrorCounter();
    }

    public void success() {
        domainDomainPullFrequencyMap.get(domainContextProvider.getCurrentDomainSafely()).sucess();
    }


}
