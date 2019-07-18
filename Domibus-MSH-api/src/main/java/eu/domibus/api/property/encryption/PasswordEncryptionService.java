package eu.domibus.api.property.encryption;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionService {

    void encryptPasswords();

    String decryptProperty(PasswordEncryptionContext passwordEncryptionContext, String propertyName, String encryptedFormatValue);

    boolean isValueEncrypted(final String propertyValue);
}
