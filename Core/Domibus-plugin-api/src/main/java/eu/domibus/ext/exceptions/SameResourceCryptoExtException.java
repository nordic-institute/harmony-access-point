package eu.domibus.ext.exceptions;

/**
 * Truststore get and upload operations Exception
 *
 * @author Soumya Chnadran
 * @since 5.1
 */
public class SameResourceCryptoExtException extends CryptoExtException {

    private final String name;

    private final String location;

    public SameResourceCryptoExtException(String name, String location, String message) {
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

