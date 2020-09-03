package eu.domibus.api.property.encryption;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionService {

    void encryptPasswords();

    void encryptPasswords(PasswordEncryptionContext passwordEncryptionContext);

    String decryptProperty(Domain domain, String propertyName, String encryptedFormatValue);

    boolean isValueEncrypted(final String propertyValue);
}
