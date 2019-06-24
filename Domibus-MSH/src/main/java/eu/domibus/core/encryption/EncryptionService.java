package eu.domibus.core.encryption;

import javax.crypto.Cipher;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface EncryptionService {

    /**
     * Creates the encryption key for all available domains if does not yet exists
     */
    void createEncryptionKeyForAllDomainsIfNotExists();

    Cipher getEncryptCipherForPayload();

    Cipher getDecryptCipherForPayload();
}
