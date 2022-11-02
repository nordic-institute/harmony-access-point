package eu.domibus.core.property.encryption;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionContextAbstract;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.core.property.DomibusRawPropertyProvider;
import eu.domibus.core.property.GlobalPropertyMetadataManager;
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
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private PasswordEncryptionService passwordEncryptionService;

    @Injectable
    private DomibusRawPropertyProvider domibusRawPropertyProvider;

    @Injectable
    private GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Tested
    private PasswordEncryptionContextDefault passwordEncryptionContextDefault;

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
            domibusRawPropertyProvider.getRawPropertyValue(myProperty);
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
    public void getEncryptedKeyFile() {
        String encryptionKeyLocation = "home" + File.separator + "location";

        new Expectations(passwordEncryptionContextDefault) {{
            passwordEncryptionContextDefault.getProperty(DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION);
            result = encryptionKeyLocation;
        }};

        final File encryptedKeyFile = passwordEncryptionContextDefault.getEncryptedKeyFile();
        assertEquals(PasswordEncryptionContextAbstract.ENCRYPTED_KEY, encryptedKeyFile.getName());
    }
    
}
