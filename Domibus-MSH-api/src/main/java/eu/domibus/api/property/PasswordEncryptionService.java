package eu.domibus.api.property;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionService {

    void encryptPasswords();

    String decryptProperty(Domain domain, String propertyName, String encryptedFormatValue);
}
