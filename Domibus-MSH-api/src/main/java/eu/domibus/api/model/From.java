package eu.domibus.api.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class From {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "FROM_ID")
    protected Set<PartyId> partyId;

    @Column(name = "FROM_ROLE")
    protected String role;

    public Set<PartyId> getPartyId() {
        if (this.partyId == null) {
            this.partyId = new HashSet<>();
        }
        return this.partyId;
    }

    public String getFirstPartyId() {
        if(this.partyId == null || this.partyId.isEmpty()) {
            return null;
        }
        return this.partyId.iterator().next().getValue();
    }

    public String getRole() {
        return this.role;
    }

    /**
     * Sets the value of the role property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRole(final String value) {
        this.role = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof From)) return false;

        final From from = (From) o;

        if (!this.partyId.equals(from.getPartyId())) return false;
        return !(this.role != null ? !this.role.equalsIgnoreCase(from.role) : from.role != null);

    }

    @Override
    public int hashCode() {
        int result = this.partyId.hashCode();
        result = 31 * result + (this.role != null ? this.role.hashCode() : 0);
        return result;
    }
}
