package eu.domibus.core.property.listeners;

import eu.domibus.common.validators.GatewayConfigurationValidator;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityPropertiesChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    MultiDomainCryptoService cryptoService;

    @Autowired
    GatewayConfigurationValidator gatewayConfigurationValidator;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.security.keystore.location");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        cryptoService.refresh();

        gatewayConfigurationValidator.validateConfiguration();
    }
}
