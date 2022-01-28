package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.KeyStoreType;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.property.GatewayConfigurationValidator;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Handles the change of DOMIBUS_SECURITY_KEYSTORE_LOCATION property
 */
@Service
public class TruststoreChangeListener implements DomibusPropertyChangeListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(TruststoreChangeListener.class);

    private final MultiDomainCryptoService multiDomainCryptoService;

    private final DomainService domainService;

    private final GatewayConfigurationValidator gatewayConfigurationValidator;

    private final DomibusPropertyProvider domibusPropertyProvider;

    public TruststoreChangeListener(MultiDomainCryptoService multiDomainCryptoService,
                                    DomainService domainService,
                                    GatewayConfigurationValidator gatewayConfigurationValidator,
                                    DomibusPropertyProvider domibusPropertyProvider) {
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.domainService = domainService;
        this.gatewayConfigurationValidator = gatewayConfigurationValidator;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_SECURITY_TRUSTSTORE_LOCATION);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.debug("[{}] property has changed for domain [{}].", propertyName, domainCode);

        Domain domain = domainService.getDomain(domainCode);
        String password = domibusPropertyProvider.getProperty(DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD);
        multiDomainCryptoService.replaceTrustStore(domain, propertyValue, password);

        multiDomainCryptoService.reset(domain, KeyStoreType.TRUSTSTORE); //??
        gatewayConfigurationValidator.validateCertificates();

    }

}
