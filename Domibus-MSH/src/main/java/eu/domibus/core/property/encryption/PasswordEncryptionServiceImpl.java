package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.property.encryption.PasswordEncryptionSecret;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.util.DomibusEncryptionException;
import eu.domibus.core.util.backup.BackupService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@Service
public class PasswordEncryptionServiceImpl implements PasswordEncryptionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionServiceImpl.class);

    public static final String ENC_START = "ENC(";
    public static final String ENC_END = ")";

    public static final String LINE_COMMENT_PREFIX = "#";
    public static final String PROPERTY_VALUE_DELIMITER = "=";

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
    protected BackupService backupService;

    @Autowired
    protected DomibusPropertyEncryptionNotifier domibusPropertyEncryptionListenerDelegate;

    @Autowired
    protected PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    public void encryptPasswords() {
        LOG.debug("Encrypting passwords");

        //operate on global context, without a current domain
        domainContextProvider.clearCurrentDomain();
        final PasswordEncryptionContextDefault passwordEncryptionContext = new PasswordEncryptionContextDefault(this, domibusPropertyProvider, domibusConfigurationService);
        encryptPasswords(passwordEncryptionContext);

        if (domibusConfigurationService.isMultiTenantAware()) {
            final List<Domain> domains = domainService.getDomains();
            for (Domain domain : domains) {
                final PasswordEncryptionContextDomain passwordEncryptionContextDomain = new PasswordEncryptionContextDomain(this, domibusPropertyProvider, domibusConfigurationService, domain);
                encryptPasswords(passwordEncryptionContextDomain);
            }
        }

        domibusPropertyEncryptionListenerDelegate.signalEncryptPasswords();

        LOG.debug("Finished encrypting passwords");
    }

    @Override
    public void encryptPasswords(PasswordEncryptionContext passwordEncryptionContext) {
        LOG.debug("Encrypting password if configured");

        final Boolean encryptionActive = passwordEncryptionContext.isPasswordEncryptionActive();
        if (!encryptionActive) {
            LOG.debug("Password encryption is not active");
            return;
        }

        final List<String> propertiesToEncrypt = passwordEncryptionContext.getPropertiesToEncrypt();
        if (CollectionUtils.isEmpty(propertiesToEncrypt)) {
            LOG.debug("No properties are needed to be encrypted");
            return;
        }

        final File encryptedKeyFile = passwordEncryptionContext.getEncryptedKeyFile();

        PasswordEncryptionSecret secret = null;
        if (encryptedKeyFile.exists()) {
            secret = passwordEncryptionDao.getSecret(encryptedKeyFile);
        } else {
            secret = passwordEncryptionDao.createSecret(encryptedKeyFile);
        }

        LOG.debug("Using encrypted key file [{}]", encryptedKeyFile);
        final SecretKey secretKey = encryptionUtil.getSecretKey(secret.getSecretKey());
        final GCMParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(secret.getInitVector());
        final List<PasswordEncryptionResult> encryptedProperties = encryptProperties(passwordEncryptionContext, propertiesToEncrypt, secretKey, secretKeySpec);

        replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);

        LOG.debug("Finished creating the encryption key");
    }

    @Override
    public boolean isValueEncrypted(final String propertyValue) {
        if (isBlank(propertyValue)) {
            return false;
        }

        return trim(propertyValue).startsWith(ENC_START);
    }


    protected List<PasswordEncryptionResult> encryptProperties(PasswordEncryptionContext passwordEncryptionContext, List<String> propertiesToEncrypt, SecretKey secretKey, GCMParameterSpec secretKeySpec) {
        List<PasswordEncryptionResult> result = new ArrayList<>();

        LOG.debug("Encrypting properties");

        for (String propertyName : propertiesToEncrypt) {
            final PasswordEncryptionResult passwordEncryptionResult = encryptProperty(passwordEncryptionContext, secretKey, secretKeySpec, propertyName);
            if (passwordEncryptionResult != null) {
                LOG.debug("Property [{}] encrypted [{}]", propertyName, passwordEncryptionResult.getFormattedBase64EncryptedValue());

                result.add(passwordEncryptionResult);
            }
        }

        return result;
    }

    @Override
    public String decryptProperty(Domain domain, String propertyName, String encryptedFormatValue) {
        final PasswordEncryptionContext passwordEncryptionContext = passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
        final File encryptedKeyFile = passwordEncryptionContext.getEncryptedKeyFile();
        return decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);
    }

    protected String decryptProperty(final File encryptedKeyFile, String propertyName, String encryptedFormatValue) {
        final boolean valueEncrypted = isValueEncrypted(encryptedFormatValue);
        if (!valueEncrypted) {
            LOG.trace("Property [{}] is not encrypted: skipping decrypting value", propertyName);
            return encryptedFormatValue;
        }

        PasswordEncryptionSecret secret = passwordEncryptionDao.getSecret(encryptedKeyFile);
        LOG.debug("Using encrypted key file for decryption [{}]", encryptedKeyFile);

        final SecretKey secretKey = encryptionUtil.getSecretKey(secret.getSecretKey());
        final GCMParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(secret.getInitVector());

        String base64EncryptedValue = extractValueFromEncryptedFormat(encryptedFormatValue);
        final byte[] encryptedValue = Base64.decodeBase64(base64EncryptedValue);

        return encryptionUtil.decrypt(encryptedValue, secretKey, secretKeySpec);
    }

    @Override
    public PasswordEncryptionResult encryptProperty(Domain domain, String propertyName, String propertyValue) {
        LOG.debug("Encrypting property [{}] for domain [{}]", propertyName, domain);

        final PasswordEncryptionContextDomain passwordEncryptionContext = new PasswordEncryptionContextDomain(this, domibusPropertyProvider, domibusConfigurationService, domain);

        final Boolean encryptionActive = passwordEncryptionContext.isPasswordEncryptionActive();
        if (!encryptionActive) {
            throw new DomibusEncryptionException(String.format("Password encryption is not active for domain [%s]", domain));
        }

        final File encryptedKeyFile = passwordEncryptionContext.getEncryptedKeyFile();
        if (!encryptedKeyFile.exists()) {
            throw new DomibusEncryptionException(String.format("Could not found encrypted key file for domain [%s]", domain));
        }

        PasswordEncryptionSecret secret = passwordEncryptionDao.getSecret(encryptedKeyFile);
        LOG.debug("Using encrypted key file [{}]", encryptedKeyFile);

        final SecretKey secretKey = encryptionUtil.getSecretKey(secret.getSecretKey());
        final GCMParameterSpec secretKeySpec = encryptionUtil.getSecretKeySpec(secret.getInitVector());
        return encryptProperty(secretKey, secretKeySpec, propertyName, propertyValue);
    }

    protected PasswordEncryptionResult encryptProperty(PasswordEncryptionContext passwordEncryptionContext, SecretKey secretKey, GCMParameterSpec secretKeySpec, String propertyName) {
        final String propertyValue = passwordEncryptionContext.getProperty(propertyName);
        return encryptProperty(secretKey, secretKeySpec, propertyName, propertyValue);
    }

    protected PasswordEncryptionResult encryptProperty(SecretKey secretKey, GCMParameterSpec secretKeySpec, String propertyName, String propertyValue) {
        if (isValueEncrypted(propertyValue)) {
            LOG.debug("Property [{}] is already encrypted", propertyName);
            return null;
        }

        final byte[] encryptedValue = encryptionUtil.encrypt(propertyValue.getBytes(), secretKey, secretKeySpec);
        final String base64EncryptedValue = Base64.encodeBase64String(encryptedValue);

        final PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName(propertyName);
        passwordEncryptionResult.setPropertyValue(propertyValue);
        passwordEncryptionResult.setBase64EncryptedValue(base64EncryptedValue);
        passwordEncryptionResult.setFormattedBase64EncryptedValue(formatEncryptedValue(base64EncryptedValue));
        return passwordEncryptionResult;
    }


    protected String formatEncryptedValue(String value) {
        return String.format(ENC_START + "%s" + ENC_END, value);
    }

    protected String extractValueFromEncryptedFormat(String encryptedFormat) {
        return StringUtils.substringBetween(encryptedFormat, ENC_START, ENC_END);
    }

    protected void replacePropertiesInFile(PasswordEncryptionContext passwordEncryptionContext, List<PasswordEncryptionResult> encryptedProperties) {
        final File configurationFile = passwordEncryptionContext.getConfigurationFile();

        LOG.debug("Replacing configured properties in file [{}] with encrypted values", configurationFile);
        final List<String> fileLines = getReplacedLines(encryptedProperties, configurationFile);

        try {
            backupService.backupFile(configurationFile);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not back up [%s]", configurationFile), e);
        }

        LOG.debug("Writing encrypted values");

        try {
            Files.write(configurationFile.toPath(), fileLines);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not write encrypted values to file [%s] ", configurationFile), e);
        }
    }

    protected List<String> getReplacedLines(List<PasswordEncryptionResult> encryptedProperties, File configurationFile) {
        try (final Stream<String> lines = Files.lines(configurationFile.toPath())) {
            return lines
                    .map(line -> replaceLine(encryptedProperties, line))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not replace properties: could not read configuration file [%s]", configurationFile), e);
        }
    }

    protected String replaceLine(List<PasswordEncryptionResult> encryptedProperties, String line) {
        if (startsWith(line, LINE_COMMENT_PREFIX) || containsNone(line, PROPERTY_VALUE_DELIMITER)) {
            return line;
        }
        String filePropertyName = trim(substringBefore(line, PROPERTY_VALUE_DELIMITER));

        if (isBlank(substringAfter(line, PROPERTY_VALUE_DELIMITER))) {
            LOG.trace("Property [{}] is empty", filePropertyName);
            return line;
        }
        final Optional<PasswordEncryptionResult> encryptedValueOptional = encryptedProperties.stream()
                .filter(encryptionResult -> arePropertiesMatching(filePropertyName, encryptionResult))
                .findFirst();
        if (!encryptedValueOptional.isPresent()) {
            LOG.trace("Property [{}] is not encrypted", filePropertyName);
            return line;
        }
        final PasswordEncryptionResult passwordEncryptionResult = encryptedValueOptional.get();
        LOG.debug("Replacing value for property [{}] with [{}]", filePropertyName, passwordEncryptionResult.getFormattedBase64EncryptedValue());

        String newLine = filePropertyName + PROPERTY_VALUE_DELIMITER + passwordEncryptionResult.getFormattedBase64EncryptedValue();
        LOG.debug("New encrypted value for property is [{}]", newLine);

        return newLine;
    }

    protected boolean arePropertiesMatching(String filePropertyName, PasswordEncryptionResult encryptionResult) {
        return StringUtils.contains(filePropertyName, encryptionResult.getPropertyName());
    }

}
