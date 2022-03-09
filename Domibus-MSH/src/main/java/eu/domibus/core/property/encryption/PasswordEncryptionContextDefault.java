package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.DomibusRawPropertyProvider;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextDefault extends PasswordEncryptionContextAbstract {

    private final DomibusRawPropertyProvider domibusRawPropertyProvider;

    public PasswordEncryptionContextDefault(PasswordEncryptionService passwordEncryptionService,
                                            DomibusPropertyProvider domibusPropertyProvider,
                                            DomibusConfigurationService domibusConfigurationService,
                                            DomibusRawPropertyProvider domibusRawPropertyProvider) {
        super(passwordEncryptionService, domibusPropertyProvider, domibusConfigurationService);
        this.domibusRawPropertyProvider = domibusRawPropertyProvider;
    }

    @Override
    public boolean isPasswordEncryptionActive() {
        return domibusConfigurationService.isPasswordEncryptionActive();
    }

    @Override
    public String getProperty(String propertyName) {
        return domibusRawPropertyProvider.getRawPropertyValue(propertyName);
    }

    @Override
    protected String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName();
    }
}
