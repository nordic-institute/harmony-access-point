package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionExtService {

    void encryptPasswordsInFile(PluginPasswordEncryptionContext pluginPasswordEncryptionContext);

    boolean isValueEncrypted(final String propertyValue);

    String decryptProperty(DomainDTO domain, String propertyName, String encryptedFormatValue);
}
