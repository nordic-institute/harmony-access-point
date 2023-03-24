package eu.domibus.api.crypto;

/**
 * @author Ion Perpgel
 * @since 5.1
 */
public class SameResourceCryptoException extends CryptoException {
    private String name;

    private String location;

    public SameResourceCryptoException(String name, String location, String message) {
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