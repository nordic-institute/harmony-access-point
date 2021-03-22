package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of crypto related properties
 */
@Service
public class CryptoChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected MultiDomainCryptoService cryptoService;

    @Autowired
    protected GatewayConfigurationValidator gatewayConfigurationValidator;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithAny(propertyName,
                DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_KEYSTORE_PREFIX,
                DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_TRUSTSTORE_PREFIX)
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        cryptoService.reset();

        gatewayConfigurationValidator.validateConfiguration();
    }
}
