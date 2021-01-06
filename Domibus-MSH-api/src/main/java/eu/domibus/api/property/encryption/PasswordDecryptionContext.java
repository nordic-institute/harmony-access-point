package eu.domibus.api.property.encryption;

import java.io.File;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordDecryptionContext {

//    boolean isPasswordEncryptionActive();
//
    String getProperty(String propertyName);
//
//    File getConfigurationFile();
//
//    List<String> getPropertiesToEncrypt();

    File getEncryptedKeyFile();
}
