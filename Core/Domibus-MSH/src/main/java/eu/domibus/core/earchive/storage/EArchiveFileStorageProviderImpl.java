package eu.domibus.core.earchive.storage;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveFileStorageProviderImpl implements EArchiveFileStorageProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorageProviderImpl.class);

    private final EArchiveFileStorageFactory storageFactory;

    private final DomainService domainService;

    private final DomainContextProvider domainContextProvider;

    private final DomibusPropertyProvider domibusPropertyProvider;

    protected Map<Domain, EArchiveFileStorage> instances = new HashMap<>();

    public EArchiveFileStorageProviderImpl(EArchiveFileStorageFactory storageFactory,
                                           DomainService domainService,
                                           DomainContextProvider domainContextProvider,
                                           DomibusPropertyProvider domibusPropertyProvider) {
        this.storageFactory = storageFactory;
        this.domainService = domainService;
        this.domainContextProvider = domainContextProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public void initialize() {
        createStorages();
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        createStorage(domain);
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        removeStorage(domain);
    }

    private void removeStorage(Domain domain) {
        if (!instances.containsKey(domain)) {
            LOG.info("No storage to remove for domain [{}]; exiting.", domain);
            return;
        }
        instances.remove(domain);
        LOG.info("eArchiving Storage removed for domain [{}]", domain);
    }

    private void createStorages() {
        for (Domain domain : domainService.getDomains()) {
            createStorage(domain);
        }
    }

    private void createStorage(Domain domain) {
        final Boolean eArchiveActive = domibusPropertyProvider.getBooleanProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
        if (BooleanUtils.isNotTrue(eArchiveActive)) {
            LOG.debug("eArchiving is not enabled for domain [{}], so no storage created", domain);
            return;
        }

        EArchiveFileStorage instance = storageFactory.create(domain);
        instances.put(domain, instance);
        LOG.info("eArchiving Storage initialized for domain [{}]", domain);
    }

    @Override
    public EArchiveFileStorage forDomain(Domain domain) {
        return instances.get(domain);
    }

    @Override
    public EArchiveFileStorage getCurrentStorage() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        EArchiveFileStorage currentStorage = forDomain(currentDomain);
        if (currentStorage == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001,
                    "eArchiving Storage [" + DOMIBUS_EARCHIVE_STORAGE_LOCATION + "] for domain [" + currentDomain + "] is not accessible. " +
                            "The location from the property  -> [" + domibusPropertyProvider.getProperty(currentDomain, DOMIBUS_EARCHIVE_STORAGE_LOCATION) + "]");
        }
        LOG.debug("Retrieved eArchiving Storage for domain [{}] = [{}]", currentDomain, currentStorage.getStorageDirectory());
        return currentStorage;
    }

    @Override
    public void reset(Domain domain) {
        removeStorage(domain);
        createStorage(domain);
    }
}
