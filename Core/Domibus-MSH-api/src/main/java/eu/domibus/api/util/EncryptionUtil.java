package eu.domibus.api.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.NoSuchAlgorithmException;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public interface EncryptionUtil {

    SecretKey getSecretKey(byte[] secretKey);

    GCMParameterSpec getSecretKeySpec(byte[] IV);

    /**
     * Generates a random key
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    SecretKey generateSecretKey();

    byte[] generateIV();

    Cipher getEncryptCipher(SecretKey key, GCMParameterSpec ivSpec);

    Cipher getDecryptCipher(SecretKey key, GCMParameterSpec ivSpec);

    byte[] encrypt(byte[] content, SecretKey key, GCMParameterSpec ivSpec);

    String decrypt(byte[] cipherText, SecretKey key, GCMParameterSpec ivSpec);


}
