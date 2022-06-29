package eu.domibus.api.model;

import eu.domibus.api.cache.CacheConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Entity
@Table(name = "TB_D_PARTY")
@NamedQueries({
@NamedQuery(name = "PartyId.findByValueAndType", hints = {
        @QueryHint(name = "org.hibernate.cacheRegion", value = CacheConstants.DICTIONARY_QUERIES),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select part from PartyId part where part.value=:VALUE and part.type=:TYPE"),
@NamedQuery(name = "PartyId.findByValue", hints = {
        @QueryHint(name = "org.hibernate.cacheRegion", value = CacheConstants.DICTIONARY_QUERIES),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")}, query = "select part from PartyId part where part.value=:VALUE and part.type is null")
})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PartyId extends AbstractBaseEntity implements Comparable<PartyId>, Serializable {

    @Column(name = "VALUE", unique = true)
    protected String value;

    @Column(name = "TYPE")
    protected String type;

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PartyId)) return false;
        if (!super.equals(o)) return false;

        final PartyId partyId = (PartyId) o;

        if (this.type != null ? !this.type.equalsIgnoreCase(partyId.type) : partyId.type != null) return false;
        return this.value.equalsIgnoreCase(partyId.value);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.value.hashCode();
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final PartyId o) {
        return this.hashCode() - o.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("type", type)
                .toString();
    }
}
