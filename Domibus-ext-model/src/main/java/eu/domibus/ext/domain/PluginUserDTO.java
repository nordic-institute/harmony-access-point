package eu.domibus.ext.domain;

import eu.domibus.ext.domain.security.AuthType;
import org.apache.commons.lang3.StringUtils;

/**
 * Data Object for Plugin User management using External API
 *
 * @author Arun Raj
 * @since 5.0
 */
public class PluginUserDTO {

    private String userName;

    private String password;

    private String certificateId; //TODO: Should we support this from /ext services?

    private String originalUser;

    private String authRoles;

    private String authenticationType; // TODO: can we default this to AuthType.BASIC for /ext services?

    private String status;

    private boolean active;

    private boolean suspended;

    private String domain;

    /**
     * Returns the name of the user but only for basic type of users
     * @return the name
     */
    public String getUserName() {
        return StringUtils.equals(AuthType.BASIC.name(), authenticationType) ? userName : null;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the id of the certificate of the user but only for certificate type of users
     * @return the certificate id
     */
    public String getCertificateId() {
        return StringUtils.equals(AuthType.CERTIFICATE.name(), authenticationType) ? certificateId : null;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public String getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(String authRoles) {
        this.authRoles = authRoles;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

}
