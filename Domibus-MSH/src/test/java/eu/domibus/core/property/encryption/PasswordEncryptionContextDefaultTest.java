package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class PasswordEncryptionContextDefaultTest {

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Tested
    PasswordEncryptionContextDefault passwordEncryptionContextDefault;

    @Test
    public void isPasswordEncryptionActive() {
        new Expectations() {{
            domibusConfigurationService.isPasswordEncryptionActive();
            result = true;
        }};

        Assert.assertTrue(passwordEncryptionContextDefault.isPasswordEncryptionActive());
    }

    @Test
    public void getProperty() {
        String myProperty = "myProperty";
        passwordEncryptionContextDefault.getProperty(myProperty);

        new Verifications() {{
            domibusPropertyProvider.getProperty(myProperty);
        }};
    }

    @Test
    public void getConfigurationFileName() {
        String myConfFile = "myConfFile";

        new Expectations() {{
            domibusConfigurationService.getConfigurationFileName();
            result = myConfFile;
        }};

        Assert.assertEquals(myConfFile, passwordEncryptionContextDefault.getConfigurationFileName());
    }

    @Test
    public void getEncryptedKeyFile() throws IOException {
        String encryptionKeyLocation = "home" + File.separator + "location";

        new Expectations(passwordEncryptionContextDefault) {{
            passwordEncryptionContextDefault.getProperty(DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION);
            result = encryptionKeyLocation;
        }};

        final File encryptedKeyFile = passwordEncryptionContextDefault.getEncryptedKeyFile();
        assertEquals(PasswordEncryptionContextAbstract.ENCRYPTED_KEY, encryptedKeyFile.getName());
    }

    @Test
    public void getPropertiesToEncrypt() {
        String propertyName1 = "property1";
        String value1 = "value1";

        String propertyName2 = "property2";
        String value2 = "value2";

        new Expectations(passwordEncryptionContextDefault) {{
            passwordEncryptionContextDefault.getProperty(DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES);
            result = propertyName1 + "," + propertyName2;

            passwordEncryptionContextDefault.getProperty(propertyName1);
            result = value1;

            passwordEncryptionContextDefault.getProperty(propertyName2);
            result = value2;
        }};

        final List<String> propertiesToEncrypt = passwordEncryptionContextDefault.getPropertiesToEncrypt();
        assertEquals(2, propertiesToEncrypt.size());
        Assert.assertTrue(propertiesToEncrypt.contains(propertyName1));
        Assert.assertTrue(propertiesToEncrypt.contains(propertyName2));
    }
}