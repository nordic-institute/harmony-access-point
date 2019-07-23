package eu.domibus.api.property.encryption;

import eu.domibus.api.multitenancy.Domain;

import java.io.File;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionService {

    void encryptPasswords();

    void encryptPasswordsIfConfigured(PasswordEncryptionContext passwordEncryptionContext);

    String decryptProperty(File encryptedKeyFile, String propertyName, String encryptedFormatValue);

    String decryptProperty(Domain domain, String propertyName, String encryptedFormatValue);

    boolean isValueEncrypted(final String propertyValue);
}
