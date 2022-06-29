package eu.domibus.api.model;

import eu.domibus.api.cache.CacheConstants;
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
@Table(name = "TB_D_MSH_ROLE")
@NamedQuery(name = "MSHRoleEntity.findByValue", hints = {
        @QueryHint(name = "org.hibernate.cacheRegion", value = CacheConstants.DICTIONARY_QUERIES),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select role from MSHRoleEntity role where role.role=:ROLE")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MSHRoleEntity extends AbstractBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", unique = true)
    protected MSHRole role;

    public MSHRole getRole() {
        return this.role;
    }

    public void setRole(final MSHRole value) {
        this.role = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MSHRoleEntity action = (MSHRoleEntity) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(role, action.role)
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
                .append("role", role)
                .toString();
    }
}
