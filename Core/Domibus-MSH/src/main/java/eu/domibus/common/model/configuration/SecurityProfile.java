package eu.domibus.common.model.configuration;


public enum SecurityProfile {
    RSA("RSA"), ECC("ECC"), NO_PROFILE("NO_PROFILE");

    private final String profile;

    SecurityProfile(final String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return this.profile;
    }
}
