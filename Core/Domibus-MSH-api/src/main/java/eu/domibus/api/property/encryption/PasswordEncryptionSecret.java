package eu.domibus.api.property.encryption;

import java.io.Serializable;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PasswordEncryptionSecret implements Serializable {

    protected byte[] secretKey;

    protected byte[] initVector;

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public byte[] getInitVector() {
        return initVector;
    }

    public void setInitVector(byte[] initVector) {
        this.initVector = initVector;
    }
}
