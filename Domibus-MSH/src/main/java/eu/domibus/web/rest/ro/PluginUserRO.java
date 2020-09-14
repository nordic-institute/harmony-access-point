package eu.domibus.web.rest.ro;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.api.validators.SkipWhiteListed;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
public class PluginUserRO {

    private Integer entityId;

    @Size(min = 4, max = 255)
    @Pattern(regexp = "^[a-zA-Z0-9\\.@_]*$")
    private String userName;

    @SkipWhiteListed
    private String password;

    @CustomWhiteListed(permitted = "=,:")
    private String certificateId;

    private String originalUser;

    private String authRoles;

    private String authenticationType;

    private String status;

    private boolean active;

    private boolean suspended;

    private String domain;

    private LocalDateTime expirationDate;

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
 
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
    
    public String getCertificateId() {
        return certificateId;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    @JsonIgnore
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

}
