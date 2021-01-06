package eu.domibus.api.property.encryption;

import java.io.File;

/**
 * @author Ion perpegel
 * @since 5.0
 */
public interface PasswordDecryptionContext {

    String getProperty(String propertyName);

    File getEncryptedKeyFile();
}
