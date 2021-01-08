package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;
import eu.domibus.core.property.DomibusPropertyRetrieveManager;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public class PasswordDecryptionContextDefault extends PasswordDecryptionContextAbstract {

    protected DomibusPropertyRetrieveManager domibusPropertyRetrieveManager;

    public PasswordDecryptionContextDefault(DomibusPropertyRetrieveManager domibusPropertyRetrieveManager,
                                            DomibusConfigurationService domibusConfigurationService) {
        super(domibusConfigurationService);
        this.domibusPropertyRetrieveManager = domibusPropertyRetrieveManager;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyRetrieveManager.getInternalProperty(propertyName);
    }

    @Override
    protected String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName();
    }
}
