package eu.domibus.core.crypto.spi;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class SameResourceCryptoSpiException extends CryptoSpiException {

    private final String name;

    private final String location;

    public SameResourceCryptoSpiException(String name, String location, String message) {
        super(message);

        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }


}