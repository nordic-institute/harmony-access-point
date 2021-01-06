package eu.domibus.api.property.encryption;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface PasswordDecryptionService {

    String decryptProperty(Domain domain, String propertyName, String encryptedFormatValue);

    boolean isValueEncrypted(final String propertyValue);
}
