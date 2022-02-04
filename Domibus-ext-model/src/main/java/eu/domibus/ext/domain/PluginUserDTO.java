package eu.domibus.ext.domain;

import eu.domibus.ext.domain.security.AuthRole;

/**
 * Data Object for Plugin User management using External API
 *
 * @author Arun Raj
 * @since 5.0
 */
public class PluginUserDTO {

    private String userName;

    private String password;

    private String originalUser;

    private AuthRole authRoles;

    private boolean active;

    private boolean defaultPassword;

    private String domain;

    public String getUserName() {
        return userName;
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

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public AuthRole getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(AuthRole authRoles) {
        this.authRoles = authRoles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(boolean defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
