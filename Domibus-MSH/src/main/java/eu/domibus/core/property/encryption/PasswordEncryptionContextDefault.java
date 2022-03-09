package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextDefault extends PasswordEncryptionContextAbstract {

    public PasswordEncryptionContextDefault(PasswordEncryptionService passwordEncryptionService,
                                            DomibusPropertyProvider domibusPropertyProvider,
                                            DomibusConfigurationService domibusConfigurationService) {
        super(passwordEncryptionService, domibusPropertyProvider, domibusConfigurationService);
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
    protected String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName();
    }
}
