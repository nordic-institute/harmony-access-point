package eu.domibus.core.crypto;

import eu.domibus.api.security.SecurityProfile;
import org.apache.wss4j.common.crypto.Merlin;

import java.util.Objects;


/**
 * Keeps Security Profile data configuration corresponding to a specific alias
 *
 * @author Lucian FURCA
 * @since 5.1
 */
public class SecurityProfileAliasConfiguration {
    private String alias;

    private String password;

    private Merlin merlin;

    private SecurityProfile securityProfile;

    private String description;

    public SecurityProfileAliasConfiguration(String alias, String password, Merlin merlin, SecurityProfile securityProfile, String desc) {
        this.alias = alias;
        this.password = password;
        this.merlin = merlin;
        this.securityProfile = securityProfile;
        this.description = desc;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Merlin getMerlin() {
        return merlin;
    }

    public void setMerlin(Merlin merlin) {
        this.merlin = merlin;
    }

    public SecurityProfile getSecurityProfile() {
        return securityProfile;
    }

    public void setSecurityProfile(SecurityProfile securityProfile) {
        this.securityProfile = securityProfile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityProfileAliasConfiguration that = (SecurityProfileAliasConfiguration) o;
        return alias.equals(that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }
}
