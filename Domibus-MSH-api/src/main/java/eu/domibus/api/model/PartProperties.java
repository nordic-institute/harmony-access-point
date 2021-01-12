package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class PartProperties {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "PARTPROPERTIES_ID")
    protected Set<Property> property;

    // used by the mapper
    public Set<Property> getProperty() {
        return property;
    }

    // used by the mapper
    public void setProperty(Set<Property> property) {
        this.property = property;
    }

    public Set<Property> getProperties() {
        if (this.property == null) {
            this.property = new HashSet<>();
        }
        return this.property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartProperties that = (PartProperties) o;

        return new EqualsBuilder()
                .append(property, that.property)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(property)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("property", property)
                .toString();
    }

}
