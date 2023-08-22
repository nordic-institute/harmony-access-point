package eu.domibus.api.crypto;

import org.apache.wss4j.common.crypto.CryptoType;

import java.util.Arrays;
import java.util.Optional;

/**
 * A wrapper around WSS4J's CryptoType to allow printing its details when logging.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.1
 */
public class DomibusCryptoType {

    private final CryptoType cryptoType;

    public DomibusCryptoType(CryptoType cryptoType) {
        this.cryptoType = cryptoType;
    }

    public String asString() {
        return Optional.ofNullable(cryptoType).map(obj -> "CryptoType{" +
                "type=" + cryptoType.getType() +
                ", issuer='" + cryptoType.getIssuer() + '\'' +
                ", serial=" + cryptoType.getSerial() +
                ", bytes=" + Arrays.toString(cryptoType.getBytes()) +
                ", subjectDN='" + cryptoType.getSubjectDN() + '\'' +
                ", alias='" + cryptoType.getAlias() + '\'' +
                ", endpoint='" + cryptoType.getEndpoint() + '\'' +
                '}').orElse("null");
    }
}
