package eu.domibus.api.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.File;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public abstract class PasswordEncryptionContextAbstract implements PasswordEncryptionContext {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionContextAbstract.class);

    public static final String ENCRYPTED_KEY = "encrypted.key";

    protected PasswordEncryptionService passwordEncryptionService;
    protected DomibusConfigurationService domibusConfigurationService;
    protected DomibusPropertyProvider domibusPropertyProvider;

    public PasswordEncryptionContextAbstract(PasswordEncryptionService passwordEncryptionService,
                                             DomibusPropertyProvider domibusPropertyProvider,
                                             DomibusConfigurationService domibusConfigurationService) {
        this.passwordEncryptionService = passwordEncryptionService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public List<String> getPropertiesToEncrypt() {
        return passwordEncryptionService.getPropertiesToEncrypt(DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES, this::getProperty);
    }

    @Override
    public File getConfigurationFile() {
        final String propertyFileName = getConfigurationFileName();
        return new File(domibusConfigurationService.getConfigLocation() + File.separator + propertyFileName);
    }

    protected abstract String getConfigurationFileName();

    @Override
    public File getEncryptedKeyFile() {
        final String encryptionKeyLocation = getProperty(DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION);
        LOG.debug("Configured encryptionKeyLocation [{}]", encryptionKeyLocation);
        return new File(encryptionKeyLocation, ENCRYPTED_KEY);
    }


}
