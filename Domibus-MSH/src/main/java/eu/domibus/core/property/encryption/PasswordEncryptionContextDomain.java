package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextDomain extends PasswordEncryptionContextAbstract {

    protected Domain domain;

    public PasswordEncryptionContextDomain(PasswordEncryptionService passwordEncryptionService,
                                           DomibusPropertyProvider domibusPropertyProvider,
                                           DomibusConfigurationService domibusConfigurationService,
                                           Domain domain) {
        super(passwordEncryptionService, domibusPropertyProvider, domibusConfigurationService);
        this.domain = domain;
    }

    @Override
    public boolean isPasswordEncryptionActive() {
        return domibusConfigurationService.isPasswordEncryptionActive(domain);
    }

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyProvider.getProperty(domain, propertyName);
    }

    @Override
    public String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName(domain);
    }

}
