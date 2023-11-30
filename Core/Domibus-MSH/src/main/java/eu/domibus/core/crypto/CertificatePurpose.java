package eu.domibus.core.crypto;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
public enum CertificatePurpose {
    SIGN("SIGN"),
    ENCRYPT("ENCRYPT"),
    DECRYPT("DECRYPT");

    private static final Map<String, CertificatePurpose> nameIndex =
            Maps.newHashMapWithExpectedSize(CertificatePurpose.values().length);
    static {
        for (CertificatePurpose certPurpose : CertificatePurpose.values()) {
            nameIndex.put(certPurpose.name(), certPurpose);
        }
    }

    public static CertificatePurpose lookupByName(String name) {
        return nameIndex.get(name);
    }

    private final String certificatePurpose;

    CertificatePurpose(final String certificatePurpose) {
        this.certificatePurpose = certificatePurpose;
    }

    public String getCertificatePurpose() {
        return this.certificatePurpose;
    }
}