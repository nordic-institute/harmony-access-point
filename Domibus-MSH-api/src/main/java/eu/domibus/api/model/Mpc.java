package eu.domibus.api.model;

import eu.domibus.api.ebms3.Ebms3Constants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@NamedQueries({
        @NamedQuery(name = "Mpc.findByValue", query = "select mpc from Mpc mpc where mpc.value=:MPC"),
})
@Entity
@Table(name = "TB_D_MPC")
public class Mpc extends AbstractBaseEntity {

    @Column(name = "VALUE", unique = true)
    protected String value = Ebms3Constants.DEFAULT_MPC;

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Mpc mpc = (Mpc) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(value, mpc.value)
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
