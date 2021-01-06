package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordDecryptionService;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public class PasswordDecryptionContextDefault extends PasswordDecryptionContextAbstract {

    protected DomibusPropertyProvider domibusPropertyProvider;

    public PasswordDecryptionContextDefault(PasswordDecryptionService passwordDecryptionService,
                                            DomibusPropertyProvider domibusPropertyProvider,
                                            DomibusConfigurationService domibusConfigurationService) {
        super(passwordDecryptionService, domibusConfigurationService);
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
