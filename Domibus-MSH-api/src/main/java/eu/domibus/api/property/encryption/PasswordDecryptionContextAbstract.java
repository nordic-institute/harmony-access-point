package eu.domibus.api.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public abstract class PasswordDecryptionContextAbstract implements PasswordDecryptionContext {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordDecryptionContextAbstract.class);

    public static final String ENCRYPTED_KEY = "encrypted.key";

    protected PasswordDecryptionService passwordEncryptionService;
    protected DomibusConfigurationService domibusConfigurationService;

    public PasswordDecryptionContextAbstract(PasswordDecryptionService passwordEncryptionService,
                                             DomibusConfigurationService domibusConfigurationService) {
        this.passwordEncryptionService = passwordEncryptionService;
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
