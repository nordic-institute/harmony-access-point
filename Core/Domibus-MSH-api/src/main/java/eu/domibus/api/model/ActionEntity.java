package eu.domibus.api.model;

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
@Table(name = "TB_D_ACTION")
@NamedQuery(name = "Action.findByValue", hints = {
        @QueryHint(name = "org.hibernate.cacheRegion", value = "dictionary-queries"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")},
        query = "select serv from ActionEntity serv where serv.value=:VALUE")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ActionEntity extends AbstractBaseEntity {

    @Column(name = "ACTION", unique = true)
    protected String value;

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

        ActionEntity action = (ActionEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(value, action.value)
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
