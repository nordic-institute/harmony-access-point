package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.PasswordEncryptionContext;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextDefault implements PasswordEncryptionContext {

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected DomibusConfigurationService domibusConfigurationService;

    public PasswordEncryptionContextDefault(DomibusPropertyProvider domibusPropertyProvider, DomibusConfigurationService domibusConfigurationService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public boolean isPasswordEncryptionActive() {
        return domibusConfigurationService.isPasswordEncryptionActive();
    }

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName();
    }
}
