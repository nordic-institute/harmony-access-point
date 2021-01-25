package eu.domibus.api.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class MessageProperties {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "MESSAGEPROPERTIES_ID")
    protected Set<Property> property;

    public Set<Property> getProperty() {
        if (this.property == null) {
            this.property = new HashSet<>();
        }
        return this.property;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageProperties)) return false;

        final MessageProperties that = (MessageProperties) o;

        return !(this.property != null ? !this.property.equals(that.property) : that.property != null);

    }

    @Override
    public int hashCode() {
        return this.property != null ? this.property.hashCode() : 0;
    }
}
