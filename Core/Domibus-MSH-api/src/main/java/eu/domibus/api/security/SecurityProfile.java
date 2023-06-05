package eu.domibus.api.security;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
public enum SecurityProfile {
    RSA("RSA"),
    ECC("ECC");

    private final String profile;

    SecurityProfile(final String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return this.profile;
    }
}
