package eu.domibus.core.encryption;

import javax.crypto.Cipher;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface EncryptionService {

    /**
     * Creates the payload encryption key for all available domains if does not yet exists
     */
    void createPayloadEncryptionKeyForAllDomainsIfNotExists();

    /**
     * Creates the password encryption key for all available domains if does not yet exists
     */
    void createPasswordEncryptionKeyForAllDomainsIfNotExists();

    Cipher getEncryptCipherForPayload();

    Cipher getDecryptCipherForPayload();
}
