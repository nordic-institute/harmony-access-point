package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_KEYSTORE_LOCATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_KEYSTORE_PASSWORD;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Handles the change of DOMIBUS_SECURITY_KEYSTORE_LOCATION property
 */
@Service
public class KeystoreLocationChangeListener implements DomibusPropertyChangeListener {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(KeystoreLocationChangeListener.class);

    private final MultiDomainCryptoService multiDomainCryptoService;

    private final DomainService domainService;

    public KeystoreLocationChangeListener(MultiDomainCryptoService multiDomainCryptoService,
                                          DomainService domainService) {
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.domainService = domainService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName, DOMIBUS_SECURITY_KEYSTORE_LOCATION);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.debug("[{}] property has changed for domain [{}]. Deleting the existing one and saving the new one in the DB."
                , DOMIBUS_SECURITY_KEYSTORE_LOCATION, domainCode);

        multiDomainCryptoService.replaceKeyStore(domainService.getDomain(domainCode), propertyValue);
    }

}
