package eu.domibus.common.model.configuration;


public enum SecurityProfile {
    RSA("RSA", AsymmetricSignatureAlgorithm.RSA_SHA256.getAlgorithm()),
    ECC("ECC", AsymmetricSignatureAlgorithm.ECC_SHA256.getAlgorithm());

    private final String profile;

    private final String algorithm;

    SecurityProfile(final String profile, String algorithm) {
        this.profile = profile;
        this.algorithm = algorithm;
    }

    public String getProfile() {
        return this.profile;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

}
