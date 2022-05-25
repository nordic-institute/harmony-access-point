package eu.domibus.core.property.encryption;

import eu.domibus.api.property.encryption.PasswordEncryptionSecret;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.util.DomibusEncryptionException;
import eu.domibus.core.util.EncryptionUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class PasswordEncryptionDaoImpl implements PasswordEncryptionDao {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PasswordEncryptionDaoImpl.class);

    protected final EncryptionUtil encryptionUtil;

    public PasswordEncryptionDaoImpl(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    @Cacheable(value = "encryptionKey", key = "#encryptedKeyFile.getCanonicalPath()")
    @Override
    public PasswordEncryptionSecret getSecret(File encryptedKeyFile) {
        LOG.debug("Getting the secret key from file [{}]", encryptedKeyFile);

        String fileContent ;
        try {
            fileContent = FileUtils.readFileToString(encryptedKeyFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not read secret key from file [%s] ", encryptedKeyFile), e);
        }
        final byte[] decodeBase64 = Base64.decodeBase64(fileContent);
        int ivBytesLength = EncryptionUtilImpl.INIT_VECTOR_LENGTH;
        final byte[] ivBytes = new byte[ivBytesLength];
        final int secretKeyLength = decodeBase64.length - ivBytesLength;
        final byte[] secretKeyBytes = new byte[secretKeyLength];

        LOG.debug("Copying initVector");
        System.arraycopy(decodeBase64, 0, ivBytes, 0, ivBytes.length);

        LOG.debug("Copying secretKey");
        System.arraycopy(decodeBase64, ivBytes.length, secretKeyBytes, 0, secretKeyLength);

        final PasswordEncryptionSecret passwordEncryptionSecret = new PasswordEncryptionSecret();
        passwordEncryptionSecret.setInitVector(ivBytes);
        passwordEncryptionSecret.setSecretKey(secretKeyBytes);
        return passwordEncryptionSecret;
    }


    @Override
    public PasswordEncryptionSecret createSecret(final File encryptedKeyFile) {
        final SecretKey secretKey = encryptionUtil.generateSecretKey();
        final byte[] ivBytes = encryptionUtil.generateIV();
        final byte[] secretKeyBytes = secretKey.getEncoded();
        final int IVBytesLength = ivBytes.length;
        final byte[] secret = new byte[IVBytesLength + secretKeyBytes.length];
        System.arraycopy(ivBytes, 0, secret, 0, IVBytesLength);
        System.arraycopy(secretKeyBytes, 0, secret, IVBytesLength, secretKeyBytes.length);
        final String encodeBase64String = Base64.encodeBase64String(secret);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(encodeBase64String.getBytes())) {
            FileUtils.copyToFile(inputStream, encryptedKeyFile);
        } catch (IOException e) {
            throw new DomibusEncryptionException(String.format("Could not persist secret key into [%s]", encryptedKeyFile));
        }
        final PasswordEncryptionSecret passwordEncryptionSecret = new PasswordEncryptionSecret();
        passwordEncryptionSecret.setInitVector(ivBytes);
        passwordEncryptionSecret.setSecretKey(secretKeyBytes);
        return passwordEncryptionSecret;
    }
}
