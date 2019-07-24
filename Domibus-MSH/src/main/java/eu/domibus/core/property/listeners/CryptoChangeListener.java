package eu.domibus.core.property.listeners;

import eu.domibus.common.validators.GatewayConfigurationValidator;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of crypto related properties
 */
@Service
public class CryptoChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    MultiDomainCryptoService cryptoService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.containsIgnoreCase(propertyName, "domibus.security.keystore")
                || StringUtils.equalsIgnoreCase(propertyName, "domibus.security.key.private.alias")
                || StringUtils.containsIgnoreCase(propertyName, "domibus.security.truststore");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        cryptoService.refresh();

        GatewayConfigurationValidator gatewayConfigurationValidator = applicationContext.getBean(GatewayConfigurationValidator.class);
        gatewayConfigurationValidator.validateConfiguration();
    }
}
