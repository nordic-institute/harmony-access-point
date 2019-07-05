package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.encryption.EncryptionService;
import eu.domibus.property.DomibusPropertyChangeListener;
import eu.domibus.spring.SpringContextProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayloadEncryptionPropertyChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected DomainService domainService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.payload.encryption.active");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        if (StringUtils.equalsIgnoreCase(propertyValue, "true")) {
            Domain domain = domainService.getDomain(domainCode);
            final EncryptionService encryptionService = SpringContextProvider.getApplicationContext().getBean("EncryptionServiceImpl", EncryptionService.class);
            encryptionService.createEncryptionKeyIfNotExists(domain);
        }
    }
}
