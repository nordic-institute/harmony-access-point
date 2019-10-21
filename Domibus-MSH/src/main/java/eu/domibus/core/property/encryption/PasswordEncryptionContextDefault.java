package eu.domibus.core.property.encryption;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextDefault extends PasswordEncryptionContextAbstract {

    @Autowired
    protected DomainContextProvider domainContextProvider;

    protected DomibusPropertyProvider domibusPropertyProvider;

    public PasswordEncryptionContextDefault(PasswordEncryptionService passwordEncryptionService,
                                            DomibusPropertyProvider domibusPropertyProvider,
                                            DomibusConfigurationService domibusConfigurationService) {
        super(passwordEncryptionService, domibusConfigurationService);
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;

        //operate on global context, without a current domain
        domainContextProvider.clearCurrentDomain();
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
