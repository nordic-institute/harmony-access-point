package eu.domibus.core.property.encryption;

import eu.domibus.api.property.encryption.PasswordEncryptionSecret;

import java.io.File;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PasswordEncryptionDao {

    PasswordEncryptionSecret getSecret(File encryptedKeyFile);

    PasswordEncryptionSecret createSecret(final File encryptedKeyFile);

}
