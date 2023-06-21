package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.earchive.storage.EArchiveFileStorageProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;

/**
 * @author Ion Perpegel
 * @since 5.0.5
 * <p>
 * Handles the change of eArchive storage location property
 */
@Service
public class EArchiveFileStorageChangeListener implements DomibusPropertyChangeListener {

    protected final DomainService domainService;

    protected final EArchiveFileStorageProvider eArchiveFileStorageProvider;

    public EArchiveFileStorageChangeListener(DomainService domainService, EArchiveFileStorageProvider eArchiveFileStorageProvider) {
        this.domainService = domainService;
        this.eArchiveFileStorageProvider = eArchiveFileStorageProvider;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_EARCHIVE_STORAGE_LOCATION);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);
        eArchiveFileStorageProvider.reset(domain);
    }
}
