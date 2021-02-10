package eu.domibus.api.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The REQUIRED element occurs
 * once, and contains information describing the destination party. *
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@Embeddable
public class To {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "TO_ID")
    protected Set<PartyId> partyId;

    @Column(name = "TO_ROLE")
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

    public void setRole(final String value) {
        this.role = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof To)) return false;

        final To to = (To) o;

        if (!this.partyId.equals(to.partyId)) return false;
        return !(this.role != null ? !this.role.equalsIgnoreCase(to.role) : to.role != null);

    }

    @Override
    public int hashCode() {
        int result = this.partyId.hashCode();
        result = 31 * result + (this.role != null ? this.role.hashCode() : 0);
        return result;
    }
}
