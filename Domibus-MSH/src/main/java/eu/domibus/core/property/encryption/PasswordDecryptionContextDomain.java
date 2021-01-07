package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public class PasswordDecryptionContextDomain extends PasswordDecryptionContextAbstract {

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected Domain domain;

    public PasswordDecryptionContextDomain(DomibusPropertyProvider domibusPropertyProvider,
                                           DomibusConfigurationService domibusConfigurationService,
                                           Domain domain) {
        super(domibusConfigurationService);
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domain = domain;
    }

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyProvider.getProperty(domain, propertyName);
    }

    @Override
    public String getConfigurationFileName() {
        return domibusConfigurationService.getConfigurationFileName(domain);
    }

}
