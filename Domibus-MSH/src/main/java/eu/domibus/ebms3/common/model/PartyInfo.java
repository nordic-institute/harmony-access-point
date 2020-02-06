package eu.domibus.ebms3.common.model;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This REQUIRED element occurs once,
 * and contains data about originating party and destination party.
 *
 * @author Christian Koch
 * @version 1.0
 * @since 3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PartyInfo", propOrder = {"from", "to"})
@Embeddable
public class PartyInfo {

    public static final String DIRECTION_FROM = "FROM";
    public static final String DIRECTION_TO = "TO";

    @XmlTransient
    @OneToMany(mappedBy = "userMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    protected Set<PartyId> parties;

    @XmlElement(name = "From", required = true)
    protected From from;

    @XmlElement(name = "To", required = true)
    protected To to;

    public void setFromTo() {
        if (parties == null) {
            return;
        }
        for (PartyId party : parties) {
            if (DIRECTION_FROM.equals(party.getDirection())) {
                final PartyId partyId = new PartyId();
                partyId.setValue(party.getValue());
                partyId.setType(party.getType());
                partyId.setDirection(PartyInfo.DIRECTION_FROM);
                from.getPartyId().add(partyId);
            } else if (DIRECTION_TO.equals(party.getDirection())) {
                final PartyId partyId = new PartyId();
                partyId.setValue(party.getValue());
                partyId.setType(party.getType());
                partyId.setDirection(PartyInfo.DIRECTION_TO);
                to.getPartyId().add(partyId);
            }
        }
    }

    public Set<PartyId> getParties() {
        if (this.parties == null) {
            this.parties = new HashSet<>();
        }
        return parties;
    }

    public void setParties(Set<PartyId> parties) {
        this.parties = parties;
    }

    /**
     * The REQUIRED element
     * occurs once, and contains information describing the originating party.
     *
     * @return possible object is {@link From }
     */
    public From getFrom() {
        return this.from;
    }

    /**
     * The REQUIRED element
     * occurs once, and contains information describing the originating party.
     *
     * @param value allowed object is {@link From }
     */
    public void setFrom(final From value) {
        this.from = value;
    }

    /**
     * The REQUIRED element occurs
     * once, and contains information describing the destination party.
     *
     * @return possible object is {@link To }
     */
    public To getTo() {
        return this.to;
    }

    /**
     * The REQUIRED element occurs
     * once, and contains information describing the destination party.
     *
     * @param value allowed object is {@link To }
     */
    public void setTo(final To value) {
        this.to = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PartyInfo)) return false;

        final PartyInfo partyInfo = (PartyInfo) o;

        if (this.from != null ? !this.from.equals(partyInfo.from) : partyInfo.from != null) return false;
        return !(this.to != null ? !this.to.equals(partyInfo.to) : partyInfo.to != null);

    }

    @Override
    public int hashCode() {
        int result = this.from != null ? this.from.hashCode() : 0;
        result = 31 * result + (this.to != null ? this.to.hashCode() : 0);
        return result;
    }
}
