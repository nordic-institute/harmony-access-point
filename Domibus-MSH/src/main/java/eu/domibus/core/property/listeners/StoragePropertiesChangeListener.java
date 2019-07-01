package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class StoragePropertiesChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected DomainService domainService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.attachment.storage.location");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);
        PayloadFileStorage storage = applicationContext.getBean(PayloadFileStorage.class, domain);

        storage.initFileSystemStorage();
    }
}
