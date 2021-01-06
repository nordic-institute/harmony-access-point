package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public class PasswordDecryptionContextDomain extends PasswordDecryptionContextAbstract {

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected Domain domain;

    public PasswordDecryptionContextDomain(PasswordDecryptionService passwordDecryptionService,
                                           DomibusPropertyProvider domibusPropertyProvider,
                                           DomibusConfigurationService domibusConfigurationService,
                                           Domain domain) {
        super(passwordDecryptionService, domibusConfigurationService);
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
