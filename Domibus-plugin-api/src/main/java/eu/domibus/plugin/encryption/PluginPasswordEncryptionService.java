package eu.domibus.plugin.encryption;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PluginPasswordEncryptionService {

    void encryptPasswordsInFile(PluginPasswordEncryptionContext pluginPasswordEncryptionContext);

    boolean isValueEncrypted(final String propertyValue);

    String decryptProperty(DomainDTO domain, String propertyName, String encryptedFormatValue);
}
