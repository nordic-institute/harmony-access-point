package eu.domibus.api.payload.encryption;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainsAware;

import javax.crypto.Cipher;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface PayloadEncryptionService extends DomainsAware {

    /**
     * Creates the payload encryption key for all available domains if does not yet exists
     */
    void createPayloadEncryptionKeyForAllDomainsIfNotExists();

    /**
     * Creates the encryption key for the given domain if it does not yet exist
     */
    void createPayloadEncryptionKeyIfNotExists(Domain domain);

    Cipher getEncryptCipherForPayload();

    Cipher getDecryptCipherForPayload();
}
