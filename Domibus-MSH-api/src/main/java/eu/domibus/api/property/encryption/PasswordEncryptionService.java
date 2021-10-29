package eu.domibus.api.property.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainsAware;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionService extends DomainsAware {
    boolean isValueEncrypted(final String propertyValue);

    void encryptPasswords();

    void encryptPasswords(PasswordEncryptionContext passwordEncryptionContext);

    PasswordEncryptionResult encryptProperty(Domain domain, String propertyName, String propertyValue);

}
