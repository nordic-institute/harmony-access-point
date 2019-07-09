package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.PasswordEncryptionResult;
import eu.domibus.api.property.PasswordEncryptionSecret;
import eu.domibus.api.property.PasswordEncryptionService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.util.DomibusEncryptionException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionServiceImpl.class);

    public static final String ENCRYPTED_KEY = "encrypted.key";
    public static final String ENC = "ENC(";


    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected PasswordEncryptionDao passwordEncryptionDao;

    @Autowired
    protected EncryptionUtil encryptionUtil;

    @Autowired
    protected DateUtil dateUtil;


    @Override
    public void encryptPasswords() {
        LOG.debug("Checking if password encryption is configured");

//        encryptPasswordsIfConfigured();


        if (domibusConfigurationService.isMultiTenantAware()) {
            final List<Domain> domains = domainService.getDomains();
            for (Domain domain : domains) {
                encryptPasswordsIfConfigured(domain);
            }
        }

        LOG.debug("Finished checking if password encryption is configured");
    }

    protected void encryptPasswordsIfConfigured() {
        encryptPasswordsIfConfigured(null);
    }

    protected List<String> getPropertiesToEncrypt(Domain domain) {
        final String propertiesToEncryptString = domibusPropertyProvider.getProperty(domain, "domibus.password.encryption.properties");
        if (StringUtils.isEmpty(propertiesToEncryptString)) {
            LOG.debug("No properties to encrypt for domain [{}]", domain);
            return new ArrayList<>();
        }
        final String[] propertiesToEncrypt = StringUtils.split(propertiesToEncryptString, ",");
        return Arrays.asList(propertiesToEncrypt);
    }

    protected void encryptPasswordsIfConfigured(Domain domain) {
        LOG.debug("Checking if the encryption key should be created");

        final Boolean encryptionActive = domibusConfigurationService.isPasswordEncryptionActive(domain);
        if (!encryptionActive) {
            LOG.debug("Password encryption is not activated");
            return;
        }

        final File encryptedKeyFile = getEncryptedKeyFile(domain);

        PasswordEncryptionSecret secret = null;
        if (encryptedKeyFile.exists()) {
            secret = passwordEncryptionDao.getSecret(encryptedKeyFile);
        } else {
            secret = passwordEncryptionDao.createSecret(encryptedKeyFile);
        }

        LOG.debug("Using encrypted key file [{}]", encryptedKeyFile);
        final SecretKey secretKey = encryptionUtil.getSecretKey(secret.getSecretKey());
        final IvParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(secret.getInitVector());
        final List<String> propertiesToEncrypt = getPropertiesToEncrypt(domain);
        final List<PasswordEncryptionResult> encryptedProperties = encryptProperties(domain, propertiesToEncrypt, secretKey, secretKeySpec);

        replaceProperty(domain, encryptedProperties);

        LOG.debug("Finished creating the encryption key");
    }

    public File getEncryptedKeyFile(Domain domain) {
        final String encryptionKeyLocation = domibusPropertyProvider.getResolvedProperty(domain, "domibus.password.encryption.key.location");
        LOG.debug("Configured encryptionKeyLocation [{}]", encryptionKeyLocation);

        return new File(encryptionKeyLocation, ENCRYPTED_KEY);
    }

    protected List<PasswordEncryptionResult> encryptProperties(Domain domain, List<String> propertiesToEncrypt, SecretKey secretKey, IvParameterSpec secretKeySpec) {
        List<PasswordEncryptionResult> result = new ArrayList<>();

        LOG.debug("Encrypting properties");

        for (String propertyName : propertiesToEncrypt) {
            final PasswordEncryptionResult passwordEncryptionResult = getPasswordEncryptionResult(domain, secretKey, secretKeySpec, propertyName);
            if (passwordEncryptionResult != null) {
                LOG.debug("Property [{}] encrypted [{}]", propertyName, passwordEncryptionResult.getFormattedBase64EncryptedValue());

                result.add(passwordEncryptionResult);
            }
        }

        return result;
    }

    protected PasswordEncryptionResult getPasswordEncryptionResult(Domain domain, SecretKey secretKey, IvParameterSpec secretKeySpec, String propertyName) {
        final String propertyValue = domibusPropertyProvider.getProperty(domain, propertyName);
        if (StringUtils.trim(propertyValue).startsWith(ENC)) {
            LOG.debug("Property [{}] is already encrypted", propertyName);
            return null;
        }

        final byte[] encryptedValue = encryptionUtil.encrypt(propertyValue.getBytes(), secretKey, secretKeySpec);
        final String base64EncryptedValue = Base64.encodeBase64String(encryptedValue);

        final PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName(propertyName);
        passwordEncryptionResult.setPropertyValue(propertyValue);
        passwordEncryptionResult.setBase64EncryptedValue(base64EncryptedValue);
        passwordEncryptionResult.setFormattedBase64EncryptedValue(String.format(ENC + "%s)", base64EncryptedValue));
        return passwordEncryptionResult;
    }

    protected void replaceProperty(Domain domain, List<PasswordEncryptionResult> encryptedProperties) {
        final File configurationFile = getConfigurationFile(domain);

        LOG.debug("Replacing configured properties in file [{}] with encrypted values");

        final Stream<String> lines;
        try {
            lines = Files.lines(configurationFile.toPath());
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not replace properties: could not read configuration file [%s]", configurationFile), e);
        }

        final List<String> fileLines = lines
                .map(line -> {
                    if (!line.contains("=")) {
                        return line;
                    }
                    final String[] strings = line.split("=");
                    final String propertyName = StringUtils.trim(strings[0]);
                    if (strings.length != 2) {
                        LOG.trace("Property [{}] is empty", propertyName);
                        return line;
                    }
                    final Optional<PasswordEncryptionResult> encryptedValueOptional = encryptedProperties.stream()
                            .filter(encryptionResult -> encryptionResult.getPropertyName().equals(propertyName))
                            .findFirst();
                    if (!encryptedValueOptional.isPresent()) {
                        LOG.trace("Property [{}] is not encrypted", propertyName);
                        return line;
                    }
                    final PasswordEncryptionResult passwordEncryptionResult = encryptedValueOptional.get();
                    LOG.debug("Replacing value for property [{}] with [{}]", propertyName, passwordEncryptionResult.getFormattedBase64EncryptedValue());

                    String newLine = StringUtils.replace(line, passwordEncryptionResult.getPropertyValue(), passwordEncryptionResult.getFormattedBase64EncryptedValue());

                    return newLine;
                })
                .collect(Collectors.toList());

        LOG.debug("Writing encrypted values ");

        final File configurationFileBackup = getConfigurationFileBackup(configurationFile);

        LOG.debug("Backing up file [{}] to file [{}]", configurationFile, configurationFileBackup);
        try {
            FileUtils.copyFile(configurationFile, configurationFileBackup);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not back up file [%s] to [%s]", configurationFile, configurationFileBackup), e);
        }

        try {
            Files.write(configurationFile.toPath(), fileLines);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not write encrypted values to file [%s] ", configurationFile), e);
        }
    }

    protected File getConfigurationFileBackup(File configurationFile) {
        return new File(configurationFile.getParent(), configurationFile.getName() + ".backup-" + dateUtil.getCurrentTime());
    }

    protected File getConfigurationFile(Domain domain) {
        final String propertyFileName = getConfigurationFileName(domain);
        return new File(domibusConfigurationService.getConfigLocation() + File.separator + propertyFileName);
    }

    protected String getConfigurationFileName(Domain domain) {
        String propertyFileName = null;
        if (domain == null) {
            LOG.debug("Using property file [{}]", DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE);
            propertyFileName = DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE;
        } else if (DomainService.DEFAULT_DOMAIN.equals(domain)) {
            final String configurationFile = domibusConfigurationService.getConfigLocation() + File.separator + getDomainConfigurationFileName(DomainService.DEFAULT_DOMAIN);
            LOG.debug("Checking if file [{}] exists", configurationFile);
            if (new File(configurationFile).exists()) {
                LOG.debug("Using property file [{}]", configurationFile);
                propertyFileName = configurationFile;
            } else {
                LOG.debug("File [{}] does not exists, using [{}]", configurationFile, DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE);
                propertyFileName = DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE;
            }
        } else {
            propertyFileName = getDomainConfigurationFileName(domain);
            LOG.debug("Using property file [{}]", propertyFileName);
        }

        return propertyFileName;
    }

    protected String getDomainConfigurationFileName(Domain domain) {
        return domain.getCode() + "-" + DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE;
    }

}
