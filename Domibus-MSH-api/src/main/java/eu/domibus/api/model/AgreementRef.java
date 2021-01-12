package eu.domibus.api.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class AgreementRef {

    @Column(name = "AGREEMENT_REF_VALUE")
    protected String value;

    @Column(name = "AGREEMENT_REF_TYPE")
    protected String type;

    @Column(name = "AGREEMENT_REF_PMODE")
    protected String pmode;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    public String getPmode() {
        return this.pmode;
    }

    public void setPmode(final String value) {
        this.pmode = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AgreementRef)) return false;

        final AgreementRef that = (AgreementRef) o;

        if (this.pmode != null ? !this.pmode.equalsIgnoreCase(that.pmode) : that.pmode != null) return false;
        if (this.type != null ? !this.type.equalsIgnoreCase(that.type) : that.type != null) return false;
        return !(this.value != null ? !this.value.equalsIgnoreCase(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = this.value != null ? this.value.hashCode() : 0;
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        result = 31 * result + (this.pmode != null ? this.pmode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .append("type", type)
                .append("pmode", pmode)
                .toString();
    }
}
