package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.encryption.PasswordDecryptionContext;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.api.property.encryption.PasswordEncryptionSecret;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class PasswordDecryptionServiceImpl implements PasswordDecryptionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordDecryptionServiceImpl.class);

    public static final String ENC_START = "ENC(";
    public static final String ENC_END = ")";

    @Autowired
    protected PasswordEncryptionDao passwordEncryptionDao;

    @Autowired
    protected EncryptionUtil encryptionUtil;

    @Autowired
    protected PasswordDecryptionContextFactory passwordEncryptionContextFactory;

    @Override
    public boolean isValueEncrypted(final String propertyValue) {
        if (isBlank(propertyValue)) {
            return false;
        }

        return trim(propertyValue).startsWith(ENC_START);
    }

    @Override
    public String decryptProperty(Domain domain, String propertyName, String encryptedFormatValue) {
        final PasswordDecryptionContext passwordEncryptionContext = passwordEncryptionContextFactory.getContext(domain);
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

    protected String extractValueFromEncryptedFormat(String encryptedFormat) {
        return StringUtils.substringBetween(encryptedFormat, ENC_START, ENC_END);
    }

}
