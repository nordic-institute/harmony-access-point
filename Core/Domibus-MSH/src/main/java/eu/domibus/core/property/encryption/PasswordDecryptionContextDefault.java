package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;
import eu.domibus.core.property.PropertyRetrieveManager;

/**
 * Data context for decrypting properties when domain is null(global and super properties)
 *
 * @author Ion perpegel
 * @since 5.0
 */
public class PasswordDecryptionContextDefault extends PasswordDecryptionContextAbstract {

    protected PropertyRetrieveManager propertyRetrieveManager;

    public PasswordDecryptionContextDefault(PropertyRetrieveManager propertyRetrieveManager,
                                            DomibusConfigurationService domibusConfigurationService) {
        super(domibusConfigurationService);
        this.propertyRetrieveManager = propertyRetrieveManager;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public String getProperty(String propertyName) {
        return propertyRetrieveManager.getInternalProperty(propertyName);
    }

    @Override
    protected String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName();
    }
}
