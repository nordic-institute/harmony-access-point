package eu.domibus.core.payload.encryption;

import javax.crypto.Cipher;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface PayloadEncryptionService {

    /**
     * Creates the payload encryption key for all available domains if does not yet exists
     */
    void createPayloadEncryptionKeyForAllDomainsIfNotExists();

    Cipher getEncryptCipherForPayload();

    Cipher getDecryptCipherForPayload();
}
