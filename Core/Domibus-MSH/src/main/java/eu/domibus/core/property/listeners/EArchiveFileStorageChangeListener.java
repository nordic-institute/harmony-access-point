package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;

/**
 * @author Ion Perpegel
 * @since 5.0.5
 * <p>
 * Handles the change of eArchive storage location property
 */
@Service
public class EArchiveFileStorageChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveFileStorageChangeListener.class);

    protected final DomainService domainService;

    protected final EArchiveFileStorageProvider eArchiveFileStorageProvider;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    public EArchiveFileStorageChangeListener(DomainService domainService, EArchiveFileStorageProvider eArchiveFileStorageProvider, DomibusPropertyProvider domibusPropertyProvider) {
        this.domainService = domainService;
        this.eArchiveFileStorageProvider = eArchiveFileStorageProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_EARCHIVE_STORAGE_LOCATION, DOMIBUS_EARCHIVE_ACTIVE);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        if (StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_EARCHIVE_STORAGE_LOCATION)) {
            onStorageLocationChanged(domain);
        }
        if (StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_EARCHIVE_ACTIVE)) {
            onEArchivingActiveChanged(domain);
        }
    }

    private void onStorageLocationChanged(Domain domain) {
        eArchiveFileStorageProvider.reset(domain);
    }

    private void onEArchivingActiveChanged(Domain domain) {
        Boolean active = domibusPropertyProvider.getBooleanProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
        String storageLocation = domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_STORAGE_LOCATION);

        if (BooleanUtils.isTrue(active)) {
            if (StringUtils.isBlank(storageLocation)) {
                throw new DomibusPropertyException("EArchiving cannot be activated for domain " + domain.getCode() + " because the storage location is not set");
            }

            LOG.debug("EArchiving for domain [{}] is being activated with storage location [{}]", domain.getCode(), storageLocation);
            eArchiveFileStorageProvider.reset(domain);
        }
    }
}
