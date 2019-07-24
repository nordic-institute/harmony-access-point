package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.spring.SpringContextProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of payload encryption property
 */
@Service
public class PayloadEncryptionChangeListener implements PluginPropertyChangeListener {

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
            final PayloadEncryptionService payloadEncryptionService = SpringContextProvider.getApplicationContext().getBean("EncryptionServiceImpl", PayloadEncryptionService.class);
            payloadEncryptionService.createPayloadEncryptionKeyIfNotExists(domain);
        }
    }
}
