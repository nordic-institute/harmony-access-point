package eu.domibus.api.encryption;

/**
 * Service responsible for handling generic encryption services
 */
public interface EncryptionService {

    /**
     * Triggers any encryption logic (passwords, generating encryption keys, etc) that must happen at application startup
     */
    void handleEncryption();
}
