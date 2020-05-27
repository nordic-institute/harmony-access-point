package eu.domibus.core.payload.persistence.filesystem;

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
public class PayloadFileStorageProviderImpl implements PayloadFileStorageProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadFileStorageProviderImpl.class);

    @Autowired
    protected PayloadFileStorageFactory storageFactory;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    protected Map<Domain, PayloadFileStorage> instances = new HashMap<>();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            PayloadFileStorage instance = storageFactory.create(domain);
            instances.put(domain, instance);
            LOG.info("Storage initialized for domain [{}]", domain);
        }
    }

    @Override
    public PayloadFileStorage forDomain(Domain domain) {
        return instances.get(domain);
    }

    @Override
    public PayloadFileStorage getCurrentStorage() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        PayloadFileStorage currentStorage = forDomain(currentDomain);
        LOG.debug("Retrieved Storage for domain [{}]", currentDomain);
        if (currentStorage == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not retrieve Storage for domain" + currentDomain + " is null");
        }
        return currentStorage;
    }

    @Override
    public boolean isPayloadsPersistenceInDatabaseConfigured() {
        final PayloadFileStorage currentStorage = getCurrentStorage();
        return currentStorage.getStorageDirectory() == null || currentStorage.getStorageDirectory().getName() == null;
    }

    @Override
    public boolean isPayloadsPersistenceFileSystemConfigured() {
        return !isPayloadsPersistenceInDatabaseConfigured();
    }
}
