package eu.domibus.core.crypto;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
public enum StoreType {
    KEYSTORE("KEYSTORE"),
    TRUSTSTORE("TRUSTSTORE");

    private final String storeType;

    StoreType(final String storeType) {
        this.storeType = storeType;
    }

    public String getStoreType() {
        return this.storeType;
    }
}