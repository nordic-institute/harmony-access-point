package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class From {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_PARTY_ID_FK")
    protected PartyId partyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_ROLE_ID_FK")
    protected PartyRole role;

    public PartyId getPartyId() {
        return partyId;
    }

    public void setPartyId(PartyId partyId) {
        this.partyId = partyId;
    }

    public PartyRole getRole() {
        return role;
    }

    public String getRoleValue() {
       if(role == null) {
           return null;
       }
       return role.getValue();
    }

    public void setRole(PartyRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        From from = (From) o;

        return new EqualsBuilder()
                .append(partyId, from.partyId)
                .append(role, from.role)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(partyId)
                .append(role)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("partyId", partyId)
                .append("role", role)
                .toString();
    }
}
