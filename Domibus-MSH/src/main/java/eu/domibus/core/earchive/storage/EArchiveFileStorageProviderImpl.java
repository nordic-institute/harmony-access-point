package eu.domibus.core.earchive.storage;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class EArchiveFileStorageProviderImpl implements EArchiveFileStorageProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorageProviderImpl.class);

    @Autowired
    protected EArchiveFileStorageFactory storageFactory;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    protected Map<Domain, EArchiveFileStorage> instances = new HashMap<>();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            EArchiveFileStorage instance = storageFactory.create(domain);
            instances.put(domain, instance);
            LOG.info("EArchiving Storage initialized for domain [{}]", domain);
        }
    }

    @Override
    public EArchiveFileStorage forDomain(Domain domain) {
        return instances.get(domain);
    }

    @Override
    public EArchiveFileStorage getCurrentStorage() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        EArchiveFileStorage currentStorage = forDomain(currentDomain);
        LOG.debug("Retrieved eArchiving Storage for domain [{}]", currentDomain);
        if (currentStorage == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not retrieve eArchiving Storage for domain" + currentDomain + " is null");
        }
        return currentStorage;
    }
}
