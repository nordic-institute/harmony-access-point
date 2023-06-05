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
    protected PartyId fromPartyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_ROLE_ID_FK")
    protected PartyRole fromRole;

    public PartyId getFromPartyId() {
        return fromPartyId;
    }

    public void setFromPartyId(PartyId partyId) {
        this.fromPartyId = partyId;
    }

    public PartyRole getFromRole() {
        return fromRole;
    }

    public String getRoleValue() {
       if(fromRole == null) {
           return null;
       }
       return fromRole.getValue();
    }

    public void setFromRole(PartyRole role) {
        this.fromRole = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        From from = (From) o;

        return new EqualsBuilder()
                .append(fromPartyId, from.fromPartyId)
                .append(fromRole, from.fromRole)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(fromPartyId)
                .append(fromRole)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("partyId", fromPartyId)
                .append("role", fromRole)
                .toString();
    }
}
