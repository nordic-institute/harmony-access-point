package eu.domibus.api.model;

import eu.domibus.api.ebms3.Ebms3Constants;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_MPC")
@NamedQuery(name = "Mpc.findByValue", hints = {
        @QueryHint(name = "org.hibernate.cacheRegion", value = "dictionary-queries"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select mpc from MpcEntity mpc where mpc.value=:MPC")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MpcEntity extends AbstractBaseEntity {

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

        MpcEntity mpc = (MpcEntity) o;

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
