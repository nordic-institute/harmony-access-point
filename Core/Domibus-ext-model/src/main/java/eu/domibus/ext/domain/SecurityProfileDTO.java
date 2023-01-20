package eu.domibus.ext.domain;

import java.io.Serializable;

public class SecurityProfileDTO implements Serializable {

    private static final long serialVersionUID = -340466687847652237L;

    private SecurityProfile securityProfile;

    public SecurityProfile getSecurityProfile() {
        return securityProfile;
    }

    public void setSecurityProfile(SecurityProfile securityProfile) {
        this.securityProfile = securityProfile;
    }

    @Override
    public String toString() {
        return "SecurityProfileDTO{" +
                "securityProfile=" + securityProfile +
                '}';
    }
}