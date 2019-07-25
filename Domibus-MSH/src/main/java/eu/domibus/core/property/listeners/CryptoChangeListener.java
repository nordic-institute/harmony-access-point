package eu.domibus.core.property.listeners;

import eu.domibus.common.validators.GatewayConfigurationValidator;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS;

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
    protected MultiDomainCryptoService cryptoService;

    @Autowired
    protected GatewayConfigurationValidator gatewayConfigurationValidator;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.containsIgnoreCase(propertyName, "domibus.security.keystore")
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS)
                || StringUtils.containsIgnoreCase(propertyName, "domibus.security.truststore");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        cryptoService.refresh();

        gatewayConfigurationValidator.validateConfiguration();
    }
}
