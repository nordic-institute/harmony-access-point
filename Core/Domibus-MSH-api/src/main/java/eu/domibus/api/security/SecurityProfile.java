package eu.domibus.api.security;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
public enum SecurityProfile {
    RSA("RSA"),
    ECC("ECC");

    private static final Map<String, SecurityProfile> nameIndex =
            Maps.newHashMapWithExpectedSize(SecurityProfile.values().length);
    static {
        for (SecurityProfile secProfile : SecurityProfile.values()) {
            nameIndex.put(secProfile.name(), secProfile);
        }
    }

    public static SecurityProfile lookupByName(String name) {
        return nameIndex.get(name);
    }

    private final String profile;

    SecurityProfile(final String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return this.profile;
    }
}
