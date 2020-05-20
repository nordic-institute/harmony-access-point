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
public abstract class PasswordEncryptionContextAbstract implements PasswordEncryptionContext {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionContextAbstract.class);

    public static final String ENCRYPTED_KEY = "encrypted.key";

    protected PasswordEncryptionService passwordEncryptionService;
    protected DomibusConfigurationService domibusConfigurationService;

    public PasswordEncryptionContextAbstract(PasswordEncryptionService passwordEncryptionService,
                                             DomibusConfigurationService domibusConfigurationService) {
        this.passwordEncryptionService = passwordEncryptionService;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public List<String> getPropertiesToEncrypt() {
        final String propertiesToEncryptString = getProperty(DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES);
        if (StringUtils.isEmpty(propertiesToEncryptString)) {
            LOG.debug("No properties to encrypt");
            return new ArrayList<>();
        }
        final String[] propertiesToEncrypt = StringUtils.split(propertiesToEncryptString, ",");
        LOG.debug("The following properties are configured for encryption [{}]", Arrays.asList(propertiesToEncrypt));

        List<String> result = Arrays.stream(propertiesToEncrypt).filter(propertyName -> {
            propertyName = StringUtils.trim(propertyName);
            final String propertyValue = getProperty(propertyName);
            if (StringUtils.isBlank(propertyValue)) {
                return false;
            }

            if (!passwordEncryptionService.isValueEncrypted(propertyValue)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());


        LOG.debug("The following properties are not encrypted [{}]", result);

        return result;
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
