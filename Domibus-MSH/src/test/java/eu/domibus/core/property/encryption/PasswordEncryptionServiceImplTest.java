package eu.domibus.core.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.property.encryption.PasswordEncryptionSecret;
import eu.domibus.api.util.EncryptionUtil;
import eu.domibus.core.util.DomibusEncryptionException;
import eu.domibus.core.util.backup.BackupService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION;
import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
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
    protected BackupService backupService;

    @Injectable
    protected DomibusPropertyEncryptionNotifier domibusPropertyEncryptionListenerDelegate;

    @Injectable
    protected PasswordEncryptionContextFactory passwordEncryptionContextFactory;

    @Injectable
    DomainContextProvider domainContextProvider;

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

        new FullVerifications() {{
            domainContextProvider.clearCurrentDomain();

            domibusPropertyEncryptionListenerDelegate.signalEncryptPasswords();
            times = 1;
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

            passwordEncryptionService.encryptPasswords((PasswordEncryptionContext) any);
            times = 3;
        }};

        passwordEncryptionService.encryptPasswords();

        new FullVerifications() {{
            domainService.getDomains();
            times = 1;

            domainContextProvider.clearCurrentDomain();
            times = 3;

            domainContextProvider.setCurrentDomain(domain1);
            times = 1;

            domainContextProvider.setCurrentDomain(domain2);
            times = 1;

            domibusPropertyEncryptionListenerDelegate.signalEncryptPasswords();
            times = 1;
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
    public void isValueEncrypted_blank() {
        assertFalse(passwordEncryptionService.isValueEncrypted(""));
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

        String propertyName2 = "property2";

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

        new FullVerifications() {
        };
    }

    @Test
    public void encryptPasswordsIfConfigured_encryptedFileDoesntExists(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                                                       @Injectable File encryptedKeyFile,
                                                                       @Injectable PasswordEncryptionSecret secret,
                                                                       @Injectable byte[] secretKeyValue,
                                                                       @Injectable byte[] initVectorValue,
                                                                       @Injectable SecretKey secretKey,
                                                                       @Injectable GCMParameterSpec secretKeySpec,
                                                                       @Injectable List<PasswordEncryptionResult> encryptedProperties) {
        String propertyName1 = "property1";

        String propertyName2 = "property2";

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
            result = false;

            passwordEncryptionDao.createSecret(encryptedKeyFile);
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

        new FullVerifications() {
        };
    }

    @Test
    public void encryptPasswordsIfConfigured_propertiesToEncrypt(
            @Injectable PasswordEncryptionContext passwordEncryptionContext) {


        new Expectations(passwordEncryptionService) {{
            passwordEncryptionContext.isPasswordEncryptionActive();
            result = true;

            passwordEncryptionContext.getPropertiesToEncrypt();
            result = new ArrayList<>();
        }};

        passwordEncryptionService.encryptPasswords(passwordEncryptionContext);

        new FullVerifications() {
        };
    }

    @Test
    public void encryptPasswordsIfConfigured_isNotPasswordEncryptionActive(
            @Injectable PasswordEncryptionContext passwordEncryptionContext) {


        new Expectations(passwordEncryptionService) {{
            passwordEncryptionContext.isPasswordEncryptionActive();
            result = false;
        }};

        passwordEncryptionService.encryptPasswords(passwordEncryptionContext);

        new FullVerifications() {
        };
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

            passwordEncryptionResult1.getFormattedBase64EncryptedValue();
            result = "passwordEncryptionResult1";

            passwordEncryptionResult2.getFormattedBase64EncryptedValue();
            result = "passwordEncryptionResult2";
        }};

        final List<PasswordEncryptionResult> passwordEncryptionResults = passwordEncryptionService.encryptProperties(passwordEncryptionContext, propertiesToEncrypt, secretKey, secretKeySpec);
        assertEquals(2, passwordEncryptionResults.size());

        new FullVerifications() {
        };
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

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionContextFactory.getPasswordEncryptionContext(domain);
            result = passwordEncryptionContext;

            passwordEncryptionContext.getEncryptedKeyFile();
            result = encryptedKeyFile;

            passwordEncryptionService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);
        }};

        passwordEncryptionService.decryptProperty(domain, propertyName, encryptedFormatValue);

        new FullVerifications() {{
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

            secret.getSecretKey();
            result = "".getBytes();

            secret.getInitVector();
            result = "".getBytes();

            encryptionUtil.getSecretKey((byte[]) any);
            result = secretKey;

            encryptionUtil.getSecretKeySpec((byte[]) any);
            result = secretKeySpec;

            passwordEncryptionService.extractValueFromEncryptedFormat(encryptedFormatValue);
            result = "base64Value";

            Base64.decodeBase64("base64Value");
            result = encryptedValue;

            encryptionUtil.decrypt(encryptedValue, secretKey, secretKeySpec);
            result = "result";
        }};

        String actual = passwordEncryptionService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);

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

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionService.isValueEncrypted(encryptedFormatValue);
            result = false;
        }};

        String actual = passwordEncryptionService.decryptProperty(encryptedKeyFile, propertyName, encryptedFormatValue);

        assertEquals(encryptedFormatValue, actual);

        new FullVerifications() {
        };
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
        assertEquals(propertyName, passwordEncryptionResult.getPropertyName());
        assertEquals(propertyValue, passwordEncryptionResult.getPropertyValue());
        assertEquals("ENC(myBase64Value)", passwordEncryptionResult.getFormattedBase64EncryptedValue());

        new FullVerifications() {
        };
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

            passwordEncryptionService.getReplacedLines(encryptedProperties, configurationFile);
            result = fileLines;

            passwordEncryptionService.arePropertiesNewlyEncrypted(configurationFile, fileLines);
            result = true;
        }};

        passwordEncryptionService.replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);

        new FullVerifications() {{
            configurationFile.toString();
            backupService.backupFile(configurationFile);
            Files.write(configurationFile.toPath(), fileLines);
        }};
    }

    @Test
    public void replacePropertiesInFile_IOExceptionOnFileWrite(@Injectable PasswordEncryptionContext passwordEncryptionContext,
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

            passwordEncryptionService.getReplacedLines(encryptedProperties, configurationFile);
            result = fileLines;

            passwordEncryptionService.arePropertiesNewlyEncrypted(configurationFile, fileLines);
            result = true;

            Files.write(configurationFile.toPath(), fileLines);
            result = new IOException("TEST");
        }};

        try {
            passwordEncryptionService.replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);
            fail();
        } catch (Exception e) {
            //ok
        }

        new FullVerifications() {{
            configurationFile.toString();
            backupService.backupFile(configurationFile);
        }};
    }

    @Test
    public void replacePropertiesInFile_IOException(
            @Injectable PasswordEncryptionContext passwordEncryptionContext,
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

            passwordEncryptionService.getReplacedLines(encryptedProperties, configurationFile);
            result = fileLines;

            passwordEncryptionService.arePropertiesNewlyEncrypted(configurationFile, fileLines);
            result = true;

            backupService.backupFile(configurationFile);
            result = new IOException("TEST");
        }};

        try {
            passwordEncryptionService.replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);
            fail();
        } catch (DomibusEncryptionException e) {
            //ok
        }

        new FullVerifications() {{
            configurationFile.toString();
        }};
    }

    @Test
    public void replacePropertiesInFile_NoPropertiesEncrypted(@Injectable PasswordEncryptionContext passwordEncryptionContext,
                                                              @Injectable PasswordEncryptionResult passwordEncryptionResult,
                                                              @Injectable File configurationFile,
                                                              @Injectable List<String> fileLines) {
        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();
        encryptedProperties.add(passwordEncryptionResult);

        new Expectations(passwordEncryptionService) {{
            passwordEncryptionContext.getConfigurationFile();
            result = configurationFile;

            passwordEncryptionService.getReplacedLines(encryptedProperties, configurationFile);
            result = fileLines;

            passwordEncryptionService.arePropertiesNewlyEncrypted(configurationFile, fileLines);
            result = false;

            configurationFile.toString();
            result = "DomibusPropertiesFileName";
        }};

        passwordEncryptionService.replacePropertiesInFile(passwordEncryptionContext, encryptedProperties);

        new FullVerifications() {
        };
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
    public void replaceLine_noEqual() {
        String line = "myProperty";

        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();

        final String replacedLine = passwordEncryptionService.replaceLine(encryptedProperties, line);
        assertEquals("myProperty", replacedLine);
    }

    @Test
    public void replaceLine_noKey() {
        String line = "=value";

        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();

        final String replacedLine = passwordEncryptionService.replaceLine(encryptedProperties, line);
        assertEquals(line, replacedLine);
    }

    @Test
    public void replaceLine_empty() {
        String line = "myProperty=";

        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();

        final String replacedLine = passwordEncryptionService.replaceLine(encryptedProperties, line);
        assertEquals("myProperty=", replacedLine);
    }

    @Test
    public void replaceLineWithPropertyNameContainingPropertyValue() {
        String line = "domibus.alert.sender.smtp.password=password";
        String encryptedValue = "myEncryptedValue";

        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();
        PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName("domibus.alert.sender.smtp.password");
        passwordEncryptionResult.setPropertyValue("password");
        passwordEncryptionResult.setFormattedBase64EncryptedValue("ENC(" + encryptedValue + ")");
        encryptedProperties.add(passwordEncryptionResult);

        final String replacedLine = passwordEncryptionService.replaceLine(encryptedProperties, line);
        assertEquals("domibus.alert.sender.smtp.password=ENC(myEncryptedValue)", replacedLine);
    }

    @Test
    public void replaceLineWithPropertyNameContainingNoPropertyValue() {
        String line = "domibus.alert.sender.smtp.password=password";

        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();

        final String replacedLine = passwordEncryptionService.replaceLine(encryptedProperties, line);
        assertEquals("domibus.alert.sender.smtp.password=password", replacedLine);
    }

    @Test
    public void replaceLineWithPropertyNameContainingPropertyValueWithEquals() {
        String line = "domibus.alert.sender.smtp.password=password=1";
        String encryptedValue = "myEncryptedValue";

        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();
        PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName("domibus.alert.sender.smtp.password");
        passwordEncryptionResult.setPropertyValue("password");
        passwordEncryptionResult.setFormattedBase64EncryptedValue("ENC(" + encryptedValue + ")");
        encryptedProperties.add(passwordEncryptionResult);

        final String replacedLine = passwordEncryptionService.replaceLine(encryptedProperties, line);
        assertEquals("domibus.alert.sender.smtp.password=ENC(myEncryptedValue)", replacedLine);
    }

    @Test
    public void arePropertiesMatching() {
        String propertyName = "myProperty";
        PasswordEncryptionResult passwordEncryptionResult = new PasswordEncryptionResult();
        passwordEncryptionResult.setPropertyName("myProperty");

        assertTrue(passwordEncryptionService.arePropertiesMatching(propertyName, passwordEncryptionResult));
    }

    @Test
    public void arePropertiesNewlyEncrypted_NoChange() throws IOException {
        final List<String> replacedLines = Arrays.asList("#-----------------",
                "#domibus.deployment.clustered=false",
                "blue_gw.domibus.security.key.private.alias=blue_gw",
                "blue_gw.domibus.security.key.private.password=ENC(kI7r/YjnSp309xHU6OEzVMYQflPyQ5M=)");
        File testConfigurationFile = File.createTempFile("testDomibusProperties", null, new File("./src/test/resources/config/"));
        testConfigurationFile.deleteOnExit();
        FileUtils.writeLines(testConfigurationFile, replacedLines);

        assertFalse("No lines replaced, expect false.", passwordEncryptionService.arePropertiesNewlyEncrypted(testConfigurationFile, replacedLines));
    }

    @Test
    public void arePropertiesNewlyEncrypted_LinesChanged() throws IOException {
        final List<String> originalLines = Arrays.asList("#-----------------",
                "#domibus.deployment.clustered=false",
                "blue_gw.domibus.security.key.private.alias=blue_gw",
                "blue_gw.domibus.security.key.private.password=test123");
        final List<String> replacedLines = Arrays.asList("#-----------------",
                "#domibus.deployment.clustered=false",
                "blue_gw.domibus.security.key.private.alias=blue_gw",
                "blue_gw.domibus.security.key.private.password=ENC(kI7r/YjnSp309xHU6OEzVMYQflPyQ5M=)");

        File testConfigurationFile = File.createTempFile("testDomibusProperties", null, new File("./src/test/resources/config/"));
        testConfigurationFile.deleteOnExit();
        FileUtils.writeLines(testConfigurationFile, originalLines);

        assertTrue("Lines changed, expect true.", passwordEncryptionService.arePropertiesNewlyEncrypted(testConfigurationFile, replacedLines));
    }

    @Test
    public void arePropertiesNewlyEncrypted_ConfigFileCannotBeRead() {
        final List<String> replacedLines = Arrays.asList("#-----------------",
                "#domibus.deployment.clustered=false",
                "blue_gw.domibus.security.key.private.alias=blue_gw",
                "blue_gw.domibus.security.key.private.password=ENC(kI7r/YjnSp309xHU6OEzVMYQflPyQ5M=)");

        File configurationFile = new File("./src/test/resources/config/fileDoesNotExist.properties");
        try {
            passwordEncryptionService.arePropertiesNewlyEncrypted(configurationFile, replacedLines);
            fail("Expected DomibusEncryptionException due to file not present.");
        } catch (DomibusEncryptionException e) {
            assertTrue("Expect DomibusEncryptionException due to file not present.", e.getMessage().contains("Could not read configuration file"));
        }
    }

    @Test
    public void arePropertiesNewlyEncrypted_ReplacedLinesEmpty() throws IOException {
        final List<String> originalLines = Arrays.asList("#-----------------",
                "#domibus.deployment.clustered=false",
                "blue_gw.domibus.security.key.private.alias=blue_gw",
                "blue_gw.domibus.security.key.private.password=test123");
        final List<String> replacedLines = new ArrayList<>();

        File testConfigurationFile = File.createTempFile("testDomibusProperties", null, new File("./src/test/resources/config/"));
        testConfigurationFile.deleteOnExit();
        FileUtils.writeLines(testConfigurationFile, originalLines);

        assertFalse("Replaced lines empty, expect to consider as no change = false", passwordEncryptionService.arePropertiesNewlyEncrypted(testConfigurationFile, replacedLines));
    }

    @Test
    public void encryptProperty_ok(@Injectable PasswordEncryptionSecret passwordEncryptionSecret,
                                   @Injectable SecretKey secretKey,
                                   @Injectable GCMParameterSpec secretKeySpec) {
        PasswordEncryptionResult expected = new PasswordEncryptionResult();
        Domain domain = DomainService.DEFAULT_DOMAIN;
        byte[] secret = "secret".getBytes();
        byte[] vector = "vector".getBytes();
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        new Expectations(passwordEncryptionService) {{

            domibusConfigurationService.isPasswordEncryptionActive(domain);
            times = 1;
            result = true;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION);
            result = this.getClass().getResource("/encrypt").getPath();

            passwordEncryptionDao.getSecret((File) any);
            result = passwordEncryptionSecret;
            times = 1;

            passwordEncryptionSecret.getSecretKey();
            result = secret;

            passwordEncryptionSecret.getInitVector();
            result = vector;

            encryptionUtil.getSecretKey(secret);
            result = secretKey;
            times = 1;

            encryptionUtil.getSecretKeySpec(vector);
            result = secretKeySpec;
            times = 1;

            passwordEncryptionService.encryptProperty(secretKey, secretKeySpec, propertyName, propertyValue);
            times = 1;
            result = expected;
        }};

        PasswordEncryptionResult actual = passwordEncryptionService.encryptProperty(domain, propertyName, propertyValue);

        assertEquals(expected, actual);

        new FullVerifications() {
        };
    }

    @Test
    public void encryptProperty_noKeyFile() {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        new Expectations(passwordEncryptionService) {{

            domibusConfigurationService.isPasswordEncryptionActive(domain);
            times = 1;
            result = true;

            domibusPropertyProvider.getProperty(domain, DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION);
            result = null;

        }};

        try {
            passwordEncryptionService.encryptProperty(domain, "", "");
            fail();
        } catch (DomibusEncryptionException e) {
            //ok
        }

        new FullVerifications() {
        };
    }

    @Test
    public void encryptProperty_notActive() {
        Domain domain = DomainService.DEFAULT_DOMAIN;
        new Expectations() {{
            domibusConfigurationService.isPasswordEncryptionActive(domain);
            times = 1;
            result = false;
        }};

        try {
            passwordEncryptionService.encryptProperty(domain, "", "");
            fail();
        } catch (DomibusEncryptionException e) {
            //ok
        }

        new FullVerifications() {
        };
    }

    @Test
    public void encryptProperty_blankProperty() {
        assertNull(passwordEncryptionService.encryptProperty(null, null, "", "ENC(alreadyEncoded)"));
    }

    @Test
    public void arePropertiesNewlyEncrypted_false() {
        assertFalse(passwordEncryptionService.arePropertiesNewlyEncrypted(null, null));
    }

    @Test
    public void getReplacedLines_IOException(@Mocked File configurationFile,
                                             @Mocked Path path,
                                             @Mocked Files files) throws IOException {
        new Expectations() {{
            configurationFile.toPath();
            result = path;

            files.lines(path);
            result = new IOException("TEST");
        }};
        try {
            passwordEncryptionService.getReplacedLines(null, configurationFile);
            fail();
        } catch (DomibusEncryptionException e) {
            //ok
        }
    }

    @SuppressWarnings("AccessStaticViaInstance")
    @Test
    public void getReplacedLines_ok(@Mocked File configurationFile,
                                    @Mocked Path path,
                                    @Mocked Files files) throws IOException {
        List<PasswordEncryptionResult> encryptedProperties = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        String lines1 = "lines1";
        lines.add(lines1);
        String lines2 = "lines2";
        lines.add(lines2);

        String replacedLine1 = "replacedLine1";
        String replacedLine2 = "replacedLine2";
        new Expectations(passwordEncryptionService) {{
            configurationFile.toPath();
            result = path;

            files.lines(path);
            result = lines.stream();

            passwordEncryptionService.replaceLine(encryptedProperties, lines1);
            result = replacedLine1;
            passwordEncryptionService.replaceLine(encryptedProperties, lines2);
            result = replacedLine2;
        }};

        List<String> replacedLines = passwordEncryptionService.getReplacedLines(encryptedProperties, configurationFile);

        assertThat(replacedLines, CoreMatchers.hasItems(replacedLine1, replacedLine2));

        new FullVerifications() {
        };
    }
}