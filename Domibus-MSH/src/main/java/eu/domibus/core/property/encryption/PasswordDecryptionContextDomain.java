package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;
import eu.domibus.core.property.DomibusPropertyRetrieveManager;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public class PasswordDecryptionContextDomain extends PasswordDecryptionContextAbstract {

    protected DomibusPropertyRetrieveManager domibusPropertyRetrieveManager;

    protected Domain domain;

    public PasswordDecryptionContextDomain(DomibusPropertyRetrieveManager domibusPropertyRetrieveManager,
                                           DomibusConfigurationService domibusConfigurationService,
                                           Domain domain) {
        super(domibusConfigurationService);
        this.domibusPropertyRetrieveManager = domibusPropertyRetrieveManager;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domain = domain;
    }

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyRetrieveManager.getInternalProperty(domain, propertyName);
    }

    @Override
    public String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName(domain);
    }

}
