package eu.domibus.ext.domain;

public enum SecurityProfileDTO {
    RSA("RSA"),
    ECC("ECC");

    private final String profile;

    SecurityProfileDTO(final String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return this.profile;
    }
}