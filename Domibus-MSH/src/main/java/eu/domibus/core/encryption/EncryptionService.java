package eu.domibus.core.encryption;

import eu.domibus.api.multitenancy.Domain;

import javax.crypto.Cipher;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface EncryptionService {

    /**
     * Creates the encryption key for all available domains if does not yet exist
     */
    void createEncryptionKeyForAllDomainsIfNotExists();

    /**
     * Creates the encryption key for the given domain if it does not yet exist
     */
    void createEncryptionKeyIfNotExists(Domain domain);

    Cipher getEncryptCipherForPayload();

    Cipher getDecryptCipherForPayload();
}
