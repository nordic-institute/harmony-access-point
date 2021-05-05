package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@NamedQueries({
        @NamedQuery(name = "PartyRole.findByValue", query = "select prop from PartyRole prop where prop.value=:VALUE"),
})
@Entity
@Table(name = "TB_D_ROLE")
public class PartyRole extends AbstractBaseEntity {

    @Column(name = "ROLE")
    protected String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartyRole partyRole = (PartyRole) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(value, partyRole.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(value)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .toString();
    }
}
