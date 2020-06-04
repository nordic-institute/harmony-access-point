package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ATTACHMENT_STORAGE_LOCATION;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of attachment storage location property
 */
@Service
public class StorageChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Autowired
    PayloadFileStorageProvider payloadFileStorageProvider;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);
        PayloadFileStorage storage = payloadFileStorageProvider.forDomain(domain);

        storage.initFileSystemStorage();
    }
}
