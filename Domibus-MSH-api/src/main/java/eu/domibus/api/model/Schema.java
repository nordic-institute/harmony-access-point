
package eu.domibus.api.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class Schema {

    @Column(name = "SCHEMA_LOCATION")
    protected String location;

    @Column(name = "SCHEMA_VERSION")
    protected String version;

    @Column(name = "SCHEMA_NAMESPACE")
    protected String namespace;

    public String getLocation() {
        return this.location;
    }

    public void setLocation(final String value) {
        this.location = value;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String value) {
        this.version = value;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(final String value) {
        this.namespace = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Schema)) return false;

        final Schema schema = (Schema) o;

        if (location != null ? !location.equalsIgnoreCase(schema.location) : schema.location != null) return false;
        if (namespace != null ? !namespace.equalsIgnoreCase(schema.namespace) : schema.namespace != null) return false;
        if (version != null ? !version.equalsIgnoreCase(schema.version) : schema.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        return result;
    }
}
