package eu.domibus.api.model;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class PartyInfo {

    @Embedded
    protected From from;

    @Embedded
    protected To to;

    public From getFrom() {
        return this.from;
    }

    public void setFrom(final From value) {
        this.from = value;
    }

    public To getTo() {
        return this.to;
    }

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
