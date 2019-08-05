package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.payload.encryption.PayloadEncryptionService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.spring.SpringContextProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE;

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

    @Autowired
    protected PayloadEncryptionService payloadEncryptionService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        if (StringUtils.equalsIgnoreCase(propertyValue, "true")) {
            Domain domain = domainService.getDomain(domainCode);
            payloadEncryptionService.createPayloadEncryptionKeyIfNotExists(domain);
        }
    }
}
