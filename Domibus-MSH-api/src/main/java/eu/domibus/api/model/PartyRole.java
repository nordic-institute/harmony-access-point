package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_ROLE")
public class PartyRole extends AbstractBaseEntity {

    @Column(name = "ROLE")
    protected String role;

    public String getRole() {
        return role;
    }

    public void setRole(String value) {
        this.role = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartyRole partyRole = (PartyRole) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(role, partyRole.role)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(role)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", role)
                .toString();
    }
}
