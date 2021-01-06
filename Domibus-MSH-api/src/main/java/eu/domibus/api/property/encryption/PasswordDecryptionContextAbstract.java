package eu.domibus.api.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.File;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION;
import static eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract.ENCRYPTED_KEY;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public abstract class PasswordDecryptionContextAbstract implements PasswordDecryptionContext {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordDecryptionContextAbstract.class);

    protected PasswordDecryptionService passwordDecryptionService;

    protected DomibusConfigurationService domibusConfigurationService;

    public PasswordDecryptionContextAbstract(PasswordDecryptionService passwordDecryptionService,
                                             DomibusConfigurationService domibusConfigurationService) {
        this.passwordDecryptionService = passwordDecryptionService;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    protected abstract String getConfigurationFileName();

    @Override
    public File getEncryptedKeyFile() {
        final String encryptionKeyLocation = getProperty(DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION);
        LOG.debug("Configured encryptionKeyLocation [{}]", encryptionKeyLocation);

        return new File(encryptionKeyLocation, ENCRYPTED_KEY);
    }


}
