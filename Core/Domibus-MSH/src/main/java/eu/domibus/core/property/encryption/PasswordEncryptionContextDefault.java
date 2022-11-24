package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import eu.domibus.core.property.GlobalPropertyMetadataManager;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionContextDefault extends PasswordEncryptionContextAbstract {

    private final DomibusRawPropertyProvider domibusRawPropertyProvider;

    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    public PasswordEncryptionContextDefault(PasswordEncryptionService passwordEncryptionService,
                                            DomibusRawPropertyProvider domibusRawPropertyProvider,
                                            DomibusConfigurationService domibusConfigurationService,
                                            GlobalPropertyMetadataManager globalPropertyMetadataManager) {
        super(passwordEncryptionService, domibusConfigurationService);
        this.domibusRawPropertyProvider = domibusRawPropertyProvider;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
    }

    @Override
    public boolean isPasswordEncryptionActive() {
        return domibusConfigurationService.isPasswordEncryptionActive();
    }

    @Override
    protected Boolean handlesProperty(String propertyName) {
        DomibusPropertyMetadata propertyMetadata = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        return propertyMetadata.isEncrypted() && (!propertyMetadata.isDomain() || propertyMetadata.isWithFallback());
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
