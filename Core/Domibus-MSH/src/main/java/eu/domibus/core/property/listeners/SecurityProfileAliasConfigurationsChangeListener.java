package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.crypto.MultiDomainCryptoServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles the change of security profile alias configurations
 */

@Service
public class SecurityProfileAliasConfigurationsChangeListener implements DomibusPropertyChangeListener {

    private final DomainService domainService;

    private final MultiDomainCryptoServiceImpl multiDomainCryptoService;

    public SecurityProfileAliasConfigurationsChangeListener(DomainService domainService, MultiDomainCryptoServiceImpl multiDomainCryptoService) {
        this.domainService = domainService;
        this.multiDomainCryptoService = multiDomainCryptoService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_SECURITY_KEY_PRIVATE_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        final Domain domain = domainService.getDomain(domainCode);

        multiDomainCryptoService.resetSecurityProfiles(domain);
    }

}
