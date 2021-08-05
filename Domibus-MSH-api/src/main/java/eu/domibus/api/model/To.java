package eu.domibus.api.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_PARTY_ID_FK")
    protected PartyId toPartyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_ROLE_ID_FK")
    protected PartyRole toRole;

    public PartyId getToPartyId() {
        return toPartyId;
    }

    public void setToPartyId(PartyId partyId) {
        this.toPartyId = partyId;
    }

    public PartyRole getToRole() {
        return toRole;
    }

    public String getRoleValue() {
        if(toRole == null) {
            return null;
        }
        return toRole.getValue();
    }

    public void setToRole(PartyRole role) {
        this.toRole = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        From from = (From) o;

        return new EqualsBuilder()
                .append(toPartyId, from.fromPartyId)
                .append(toRole, from.fromRole)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(toPartyId)
                .append(toRole)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("partyId", toPartyId)
                .append("role", toRole)
                .toString();
    }
}
