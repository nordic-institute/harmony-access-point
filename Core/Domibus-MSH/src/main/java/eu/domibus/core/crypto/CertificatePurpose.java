package eu.domibus.core.crypto;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
public enum CertificatePurpose {
    SIGN("SIGN"),
    DECRYPT("DECRYPT");

    private final String certificatePurpose;

    CertificatePurpose(final String certificatePurpose) {
        this.certificatePurpose = certificatePurpose;
    }

    public String getCertificatePurpose() {
        return this.certificatePurpose;
    }
}