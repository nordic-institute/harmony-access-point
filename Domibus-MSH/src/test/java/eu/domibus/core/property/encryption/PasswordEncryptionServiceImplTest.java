package eu.domibus.core.property.encryption;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.property.encryption.PasswordEncryptionSecret;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.EncryptionUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.core.property.encryption.PasswordEncryptionServiceImpl.BACKUP_FILE_FORMATTER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class PasswordEncryptionServiceImplTest {

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
    protected DateUtil dateUtil;

    @Injectable
    protected DomibusPropertyEncryptionNotifier domibusPropertyEncryptionListenerDelegate;

    @Injectable
    protected PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    @Tested
    PasswordEncryptionServiceImpl passwordEncryptionService;

    @Test
    public void encryptPasswordsNonMultitenancy() {
        new Expectations(passwordEncryptionService) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            passwordEncryptionService.encryptPasswords((PasswordEncryptionContextDefault) any);
        }};

        passwordEncryptionService.encryptPasswords();

        new Verifications() {{
            domainService.getDomains();
            times = 0;
        }};
    }

    @Test
    public void encryptPasswordsMultitenancy(@Injectable Domain domain1,
                                             @Injectable Domain domain2) {
        List<Domain> domains = new ArrayList<>();
        domains.add(domain1);
        domains.add(domain2);

        new Expectations(passwordEncryptionService) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainService.getDomains();
            result = domains;

            passwordEncryptionService.encryptPasswords((PasswordEncryptionContextDefault) any);
            passwordEncryptionService.encryptPasswords((PasswordEncryptionContextDomain) any);
        }};

        passwordEncryptionService.encryptPasswords();

        new Verifications() {{
            domainService.getDomains();
            times = 1;

            passwordEncryptionService.encryptPasswords((PasswordEncryptionContext) any);
            times = 3;
        }};
    }

    @Test
    public void isValueEncryptedWithNonEncryptedValue() {
        Assert.assertFalse(passwordEncryptionService.isValueEncrypted("nonEncrypted"));
    }

    @Test
    public void isValueEncryptedWithEncryptedValue() {
        Assert.assertTrue(passwordEncryptionService.isValueEncrypted("ENC(nonEncrypted)"));
    }



    @Test
    public void encryptPasswordsIfConfigured(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                             @Injectable File encryptedKeyFile,
                                             @Injectable PasswordEncryptionSecret secret,
                                             @Injectable byte[] secretKeyValue,
                                             @Injectable byte[] initVectorValue,
                                             @Injectable SecretKey secretKey,
                                             @Injectable GCMParameterSpec secretKeySpec,
                                             @Injectable List<PasswordEncryptionResult> encryptedProperties) {
        String propertyName1 = "property1";
        String value1 = "value1";

        String propertyName2 = "property2";
        String value2 = "value2";

        List<String> propertiesToEncrypt = new ArrayList<>();
        propertiesToEncrypt.add(propertyName1);
        propertiesToEncrypt.add(propertyName2);

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionContext.isPasswordEncryptionActive();
            result = true;

            passwordEncryptionContext.getPropertiesToEncrypt();
            result = propertiesToEncrypt;

            passwordEncryptionContext.getEncryptedKeyFile();
            result = encryptedKeyFile;

            encryptedKeyFile.exists();
            result = true;

            passwordEncryptionDao.getSecret(encryptedKeyFile);
            result = secret;

            secret.getSecretKey();
            result = secretKeyValue;

            encryptionUtil.getSecretKey(secret.getSecretKey());
            result = secretKey;

            secret.getInitVector();
            result = initVectorValue;

            encryptionUtil.getSecretKeySpec(secret.getInitVector());
            result = secretKeySpec;

            passwordEncryptionService.encryptProperties(passwordEncryptionContext, propertiesToEncrypt, secretKey, secretKeySpec);
            result = encryptedProperties;

            passwordEncryptionService.replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);

        }};

        passwordEncryptionService.encryptPasswords(passwordEncryptionContext);
    }



    @Test
    public void encryptProperties(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                  @Injectable SecretKey secretKey,
                                  @Injectable GCMParameterSpec secretKeySpec,
                                  @Injectable PasswordEncryptionResult passwordEncryptionResult1,
                                  @Injectable PasswordEncryptionResult passwordEncryptionResult2) {
        String propertyName1 = "property1";
        String propertyName2 = "property2";

        List<String> propertiesToEncrypt = new ArrayList<>();
        propertiesToEncrypt.add(propertyName1);
        propertiesToEncrypt.add(propertyName2);

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionService.encryptProperty(passwordEncryptionContext, secretKey, secretKeySpec, propertyName1);
            result = passwordEncryptionResult1;

            passwordEncryptionService.encryptProperty(passwordEncryptionContext, secretKey, secretKeySpec, propertyName2);
            result = passwordEncryptionResult2;
        }};

        final List<PasswordEncryptionResult> passwordEncryptionResults = passwordEncryptionService.encryptProperties(passwordEncryptionContext, propertiesToEncrypt, secretKey, secretKeySpec);
        assertEquals(passwordEncryptionResults.size(), 2);
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
        byte[] encryptedValue = new byte[2];

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
            result = passwordEncryptionContext;

            passwordEncryptionContext.getEncryptedKeyFile();
            result = encryptedKeyFile;

            passwordEncryptionService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);
        }};

        passwordEncryptionService.decryptProperty(domain, propertyName, encryptedFormatValue);

        new Verifications() {{
            passwordEncryptionService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);
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

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionService.isValueEncrypted(encryptedFormatValue);
            result = true;

            passwordEncryptionDao.getSecret(encryptedKeyFile);
            result = secret;

            encryptionUtil.getSecretKey((byte[]) any);
            result = secretKey;

            encryptionUtil.getSecretKeySpec((byte[]) any);
            result = secretKeySpec;

            passwordEncryptionService.extractValueFromEncryptedFormat(encryptedFormatValue);
            result = "base64Value";

            Base64.decodeBase64("base64Value");
            result = encryptedValue;
        }};

        passwordEncryptionService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);

        new Verifications() {{
            encryptionUtil.decrypt(encryptedValue, secretKey, secretKeySpec);
        }};
    }

    @Test
    public void encryptProperty(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                @Injectable File encryptedKeyFile,
                                @Injectable PasswordEncryptionSecret secret,
                                @Injectable SecretKey secretKey,
                                @Injectable GCMParameterSpec secretKeySpec,
                                @Mocked Base64 base64) {
        String propertyName = "myProperty";
        String propertyValue = "myValue";
        byte[] encryptedValue = new byte[2];
        String base64EncryptedValue = "myBase64Value";

        new Expectations() {{
            passwordEncryptionContext.getProperty(propertyName);
            result = propertyValue;

            passwordEncryptionService.isValueEncrypted(propertyValue);
            result = false;

            encryptionUtil.encrypt((byte[]) any, secretKey, secretKeySpec);
            result = encryptedValue;

            Base64.encodeBase64String(encryptedValue);
            result = base64EncryptedValue;
        }};

        final PasswordEncryptionResult passwordEncryptionResult = passwordEncryptionService.encryptProperty(passwordEncryptionContext, secretKey, secretKeySpec, propertyName);
        assertEquals(passwordEncryptionResult.getPropertyName(), propertyName);
        assertEquals(passwordEncryptionResult.getPropertyValue(), propertyValue);
        assertEquals(passwordEncryptionResult.getFormattedBase64EncryptedValue(), "ENC(myBase64Value)");

    }

    @Test
    public void formatEncryptedValue() {
        final String myValue = passwordEncryptionService.formatEncryptedValue("myValue");
        assertEquals("ENC(myValue)", myValue);
    }

    @Test
    public void extractValueFromEncryptedFormat() {
        final String myValue = passwordEncryptionService.extractValueFromEncryptedFormat("ENC(myValue)");
        assertEquals("myValue", myValue);
    }

    @Test
    public void replacePropertiesInFile(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                        @Injectable PasswordEncryptionResult passwordEncryptionResult,
                                        @Mocked Files files,
                                        @Injectable File configurationFile,
                                        @Injectable File configurationFileBackup,
                                        @Mocked FileUtils fileUtils,
                                        @Injectable List<String> fileLines) throws IOException {
        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();
        encryptedProperties.add(passwordEncryptionResult);

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionContext.getConfigurationFile();
            result = configurationFile;

            passwordEncryptionService.getConfigurationFileBackup(configurationFile);
            result = configurationFileBackup;

            passwordEncryptionService.getReplacedLines(encryptedProperties, configurationFile);
            result = fileLines;
        }};

        passwordEncryptionService.replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);

        new Verifications() {{
            FileUtils.copyFile(configurationFile, configurationFileBackup);
            Files.write(configurationFile.toPath(), fileLines);
        }};
    }

    @Test
    public void replaceLine() {
        String line = "myProperty=myValue";
        String encryptedValue = "myEncryptedValue";

        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();
        PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName("myProperty");
        passwordEncryptionResult.setPropertyValue("myValue");
        passwordEncryptionResult.setFormattedBase64EncryptedValue("ENC(" + encryptedValue + ")");
        encryptedProperties.add(passwordEncryptionResult);

        final String replacedLine = passwordEncryptionService.replaceLine(encryptedProperties, line);
        assertEquals("myProperty=ENC(myEncryptedValue)", replacedLine);
    }

    @Test
    public void arePropertiesMatching() {
        String propertyName = "myProperty";
        PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName("myProperty");

        assertTrue(passwordEncryptionService.arePropertiesMatching(propertyName, passwordEncryptionResult));
    }

    @Test
    public void getConfigurationFileBackup() {
        String timePart = "2019-07-15_23_01_01.111";
        final String configurationFileName = "domibus.properties";
        final String parentDirectory = "home";

        new Expectations() {{
            dateUtil.getCurrentTime(BACKUP_FILE_FORMATTER);
            result = timePart;
        }};


        File configurationFile = new File(parentDirectory, configurationFileName);
        final File configurationFileBackup = passwordEncryptionService.getConfigurationFileBackup(configurationFile);
        assertEquals(configurationFileName + PasswordEncryptionServiceImpl.BACKUP_EXT + timePart, configurationFileBackup.getName());
        assertEquals(parentDirectory, configurationFileBackup.getParent());
    }

}