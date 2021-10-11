package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DynamicDomainManagementServiceImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDomainManagementServiceImpl.class);

    private List<Domain> originalDomains;

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Autowired
    private DomainService domainService;

    @Autowired
    MessageListenerContainerInitializer messageListenerContainerInitializer;

    @PostConstruct
    public void init() {
        originalDomains = domainService.getDomains();
    }

    public void handleDomainsChaned() {
        resetDomains();
        List<Domain> currentList = domainService.getDomains();
        List<Domain> addedDomains = currentList.stream()
                .filter(el -> !originalDomains.contains(el))
                .collect(Collectors.toList());
    }

    synchronized void resetDomains() {
        this.originalDomains = null;
        this.domibusCacheService.clearCache(DomibusCacheService.ALL_DOMAINS_CACHE);
    }
}
