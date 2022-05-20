package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;
import eu.domibus.core.property.PropertyRetrieveManager;

/**
 * Data context for decrypting properties when domain is not null(each domain properties)
 *
 * @author Ion perpegel
 * @since 5.0
 */
public class PasswordDecryptionContextDomain extends PasswordDecryptionContextAbstract {

    protected PropertyRetrieveManager propertyRetrieveManager;

    protected Domain domain;

    public PasswordDecryptionContextDomain(PropertyRetrieveManager propertyRetrieveManager,
                                           DomibusConfigurationService domibusConfigurationService,
                                           Domain domain) {
        super(domibusConfigurationService);
        this.propertyRetrieveManager = propertyRetrieveManager;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domain = domain;
    }

    @Override
    public String getProperty(String propertyName) {
        return propertyRetrieveManager.getInternalProperty(domain, propertyName);
    }

    @Override
    public String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName(domain);
    }

}
