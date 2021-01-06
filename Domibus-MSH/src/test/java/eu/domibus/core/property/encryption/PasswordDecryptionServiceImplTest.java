package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionSecret;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.util.backup.BackupService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class PasswordDecryptionServiceImplTest {

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected PasswordEncryptionDao passwordEncryptionDao;

    @Injectable
    protected EncryptionUtil encryptionUtil;

    @Injectable
    protected BackupService backupService;

    @Injectable
    protected DomibusPropertyEncryptionNotifier domibusPropertyEncryptionListenerDelegate;

    @Injectable
    protected PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Tested
    PasswordDecryptionServiceImpl passwordDecryptorService;

    @Test
    public void isValueEncryptedWithNonEncryptedValue() {
        Assert.assertFalse(passwordDecryptorService.isValueEncrypted("nonEncrypted"));
    }

    @Test
    public void isValueEncryptedWithEncryptedValue() {
        Assert.assertTrue(passwordDecryptorService.isValueEncrypted("ENC(nonEncrypted)"));
    }

    @Test
    public void isValueEncrypted_blank() {
        assertFalse(passwordDecryptorService.isValueEncrypted(""));
    }

    @Test
    public void decryptProperty(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                @Injectable File encryptedKeyFile,
                                @Injectable PasswordEncryptionSecret secret,
                                @Injectable SecretKey secretKey,
                                @Injectable GCMParameterSpec secretKeySpec,
                                @Mocked Base64 base64,
                                @Injectable Domain domain) {
        String propertyName = "myProperty";
        String encryptedFormatValue = PasswordEncryptionServiceImpl.ENC_START + "myValue" + PasswordEncryptionServiceImpl.ENC_END;

        new Expectations(passwordDecryptorService) {{
            passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
            result = passwordEncryptionContext;

            passwordEncryptionContext.getEncryptedKeyFile();
            result = encryptedKeyFile;

            passwordDecryptorService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);
        }};

        passwordDecryptorService.decryptProperty(domain, propertyName, encryptedFormatValue);

        new FullVerifications() {{
            passwordDecryptorService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);
            times = 1;
        }};
    }

    @Test
    public void decryptProperty1(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                 @Injectable File encryptedKeyFile,
                                 @Injectable PasswordEncryptionSecret secret,
                                 @Injectable SecretKey secretKey,
                                 @Injectable GCMParameterSpec secretKeySpec,
                                 @Mocked Base64 base64) {
        String propertyName = "myProperty";
        String encryptedFormatValue = PasswordEncryptionServiceImpl.ENC_START + "myValue" + PasswordEncryptionServiceImpl.ENC_END;
        byte[] encryptedValue = new byte[2];

        new Expectations(passwordDecryptorService) {{
            passwordDecryptorService.isValueEncrypted(encryptedFormatValue);
            result = true;

            passwordEncryptionDao.getSecret(encryptedKeyFile);
            result = secret;

            secret.getSecretKey();
            result = "".getBytes();

            secret.getInitVector();
            result = "".getBytes();

            encryptionUtil.getSecretKey((byte[]) any);
            result = secretKey;

            encryptionUtil.getSecretKeySpec((byte[]) any);
            result = secretKeySpec;

            passwordDecryptorService.extractValueFromEncryptedFormat(encryptedFormatValue);
            result = "base64Value";

            Base64.decodeBase64("base64Value");
            result = encryptedValue;

            encryptionUtil.decrypt(encryptedValue, secretKey, secretKeySpec);
            result = "result";
        }};

        String actual = passwordDecryptorService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);

        assertEquals("result", actual);

        new FullVerifications() {
        };
    }

    @Test
    public void decryptProperty_notEncrypted(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                             @Injectable File encryptedKeyFile,
                                             @Injectable PasswordEncryptionSecret secret,
                                             @Injectable SecretKey secretKey,
                                             @Injectable GCMParameterSpec secretKeySpec,
                                             @Mocked Base64 base64) {
        String propertyName = "myProperty";
        String encryptedFormatValue = PasswordEncryptionServiceImpl.ENC_START + "myValue" + PasswordEncryptionServiceImpl.ENC_END;
        byte[] encryptedValue = new byte[2];

        new Expectations(passwordDecryptorService) {{
            passwordDecryptorService.isValueEncrypted(encryptedFormatValue);
            result = false;
        }};

        String actual = passwordDecryptorService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);

        assertEquals(encryptedFormatValue, actual);

        new FullVerifications() {
        };
    }

    @Test
    public void extractValueFromEncryptedFormat() {
        final String myValue = passwordDecryptorService.extractValueFromEncryptedFormat("ENC(myValue)");
        assertEquals("myValue", myValue);
    }

}