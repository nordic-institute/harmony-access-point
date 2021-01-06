package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordDecryptionContextDefault extends PasswordDecryptionContextAbstract {

    protected DomibusPropertyProvider domibusPropertyProvider;

    public PasswordDecryptionContextDefault(PasswordDecryptionService passwordEncryptionService,
                                            DomibusPropertyProvider domibusPropertyProvider,
                                            DomibusConfigurationService domibusConfigurationService) {
        super(passwordEncryptionService, domibusConfigurationService);
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
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
