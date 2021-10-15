package eu.domibus.core.earchive.storage;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveFileStorageProviderImpl implements EArchiveFileStorageProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorageProviderImpl.class);

    protected final EArchiveFileStorageFactory storageFactory;

    protected final DomainService domainService;

    protected final DomainContextProvider domainContextProvider;

    protected Map<Domain, EArchiveFileStorage> instances = new HashMap<>();

    public EArchiveFileStorageProviderImpl(EArchiveFileStorageFactory storageFactory,
                                           DomainService domainService,
                                           DomainContextProvider domainContextProvider) {
        this.storageFactory = storageFactory;
        this.domainService = domainService;
        this.domainContextProvider = domainContextProvider;
    }

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        createStorage(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        createStorage(Arrays.asList(domain));
    }

    @Override
    public void onDomainRemoved(Domain domain) {
    }

    private void createStorage(List<Domain> domains) {
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
