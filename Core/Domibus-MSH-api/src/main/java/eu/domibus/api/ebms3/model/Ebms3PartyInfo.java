package eu.domibus.api.ebms3.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
public class Ebms3PartyInfo {

    @XmlElement(name = "From", required = true)
    protected Ebms3From from;

    @XmlElement(name = "To", required = true)
    protected Ebms3To to;

    /**
     * The REQUIRED element
     * occurs once, and contains information describing the originating party.
     *
     * @return possible object is {@link Ebms3From }
     */
    public Ebms3From getFrom() {
        return this.from;
    }

    /**
     * The REQUIRED element
     * occurs once, and contains information describing the originating party.
     *
     * @param value allowed object is {@link Ebms3From }
     */
    public void setFrom(final Ebms3From value) {
        this.from = value;
    }

    /**
     * The REQUIRED element occurs
     * once, and contains information describing the destination party.
     *
     * @return possible object is {@link Ebms3To }
     */
    public Ebms3To getTo() {
        return this.to;
    }

    /**
     * The REQUIRED element occurs
     * once, and contains information describing the destination party.
     *
     * @param value allowed object is {@link Ebms3To }
     */
    public void setTo(final Ebms3To value) {
        this.to = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Ebms3PartyInfo)) return false;

        final Ebms3PartyInfo ebms3PartyInfo = (Ebms3PartyInfo) o;

        if (this.from != null ? !this.from.equals(ebms3PartyInfo.from) : ebms3PartyInfo.from != null) return false;
        return !(this.to != null ? !this.to.equals(ebms3PartyInfo.to) : ebms3PartyInfo.to != null);

    }

    @Override
    public int hashCode() {
        int result = this.from != null ? this.from.hashCode() : 0;
        result = 31 * result + (this.to != null ? this.to.hashCode() : 0);
        return result;
    }
}
