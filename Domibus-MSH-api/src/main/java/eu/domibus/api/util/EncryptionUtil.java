package eu.domibus.api.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface EncryptionUtil {

    SecretKey getSecretKey(byte[] secretKey);

    IvParameterSpec getSecretKeySpec(byte[] IV);

    /**
     * Generates a random key
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    SecretKey generateSecretKey();

    byte[] generateIV();

    Cipher getEncryptCipher(SecretKey key, IvParameterSpec ivSpec);

    Cipher getDecryptCipher(SecretKey key, IvParameterSpec ivSpec);

    byte[] encrypt(byte[] content, SecretKey key, IvParameterSpec ivSpec);

    String decrypt(byte[] cipherText, SecretKey key, IvParameterSpec ivSpec);


}
