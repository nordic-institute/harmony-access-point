package eu.domibus.core.user.plugin;

import eu.domibus.api.security.AuthType;
import eu.domibus.core.audit.envers.RevisionLogicalName;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.user.UserEntityBaseImpl;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_AUTHENTICATION_ENTRY")
@NamedQueries({
        @NamedQuery(name = "AuthenticationEntity.findByUsername", query = "select bae from AuthenticationEntity bae where bae.userName=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.findByCertificateId", query = "select bae from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForUsername", query = "select bae.authRoles from AuthenticationEntity bae where bae.userName=:USERNAME"),
        @NamedQuery(name = "AuthenticationEntity.getRolesForCertificateId", query = "select bae.authRoles from AuthenticationEntity bae where bae.certificateId=:CERTIFICATE_ID"),
        @NamedQuery(name = "AuthenticationEntity.findWithPasswordChangedBetween", query = "FROM AuthenticationEntity ae where ae.passwordChangeDate is not null " +
                "and ae.passwordChangeDate>:START_DATE and ae.passwordChangeDate<:END_DATE " + "and ae.defaultPassword=:DEFAULT_PASSWORD"),
        @NamedQuery(name = "AuthenticationEntity.findSuspendedUsers", query = "FROM AuthenticationEntity u where u.suspensionDate is not null and u.suspensionDate<:SUSPENSION_INTERVAL")
})
@Audited(withModifiedFlag = true)
@RevisionLogicalName("PluginUser")
public class AuthenticationEntity extends UserEntityBaseImpl implements UserEntityBase {

    @Column(name = "CERTIFICATE_ID")
    private String certificateId;
    @Column(name = "USERNAME")
    private String userName;
    @Column(name = "PASSWD")
    private String password;
    @Column(name = "AUTH_ROLES")
    private String authRoles; // semicolon separated roles
    @Column(name = "ORIGINAL_USER")
    private String originalUser;
    @Column(name = "BACKEND")
    private String backend;

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        LocalDateTime changeDate = isBasic() ? LocalDateTime.now() : null;
        this.setPasswordChangeDate(changeDate);
    }

    /**
     * It returns the Name property or certificateId, depending on the identification type
     */
    @Override
    public String getUniqueIdentifier() {
        if (isBasic()) {
            return getUserName();
        }
        return getCertificateId();
    }

    public String getAuthRoles() {
        return authRoles;
    }

    public void setAuthRoles(String authRoles) {
        this.authRoles = authRoles;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    @Override
    public UserEntityBase.Type getType() {
        return Type.PLUGIN;
    }

    public boolean isBasic() {
        return StringUtils.isNotBlank(getUserName());
    }

    public boolean isSuspended() {
        return !isActive() && getSuspensionDate() != null;
    }

    public AuthType getAuthenticationType() {
        return isBasic() ? AuthType.BASIC : AuthType.CERTIFICATE;
    }
}
