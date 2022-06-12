package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.DomibusRawPropertyProvider;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextDomain extends PasswordEncryptionContextAbstract {

    protected Domain domain;

    private final DomibusRawPropertyProvider domibusRawPropertyProvider;

    public PasswordEncryptionContextDomain(PasswordEncryptionService passwordEncryptionService,
                                           DomibusRawPropertyProvider domibusRawPropertyProvider,
                                           DomibusConfigurationService domibusConfigurationService,
                                           Domain domain) {
        super(passwordEncryptionService, domibusConfigurationService);
        this.domain = domain;
        this.domibusRawPropertyProvider = domibusRawPropertyProvider;
    }

    @Override
    public boolean isPasswordEncryptionActive() {
        return domibusConfigurationService.isPasswordEncryptionActive(domain);
    }

    @Override
    public String getProperty(String propertyName) {
        return domibusRawPropertyProvider.getRawPropertyValue(domain, propertyName);
    }

    @Override
    public String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName(domain);
    }

}
