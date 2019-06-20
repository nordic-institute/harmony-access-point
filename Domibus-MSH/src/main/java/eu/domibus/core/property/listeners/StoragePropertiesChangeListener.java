package eu.domibus.core.property.listeners;

import eu.domibus.common.validators.GatewayConfigurationValidator;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoragePropertiesChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    Storage storage;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.attachment.storage.location");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        storage.initFileSystemStorage();
    }
}
