package eu.domibus.api.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
@Embeddable
public class Description {

    @Column(name = "DESCRIPTION_VALUE")
    protected String value;

    @Column(name = "DESCRIPTION_LANG")
    protected String lang;

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getLang() {
        return this.lang;
    }
    public void setLang(final String value) {
        this.lang = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Description)) return false;

        final Description that = (Description) o;

        if (this.lang != null ? !this.lang.equals(that.lang) : that.lang != null) return false;
        return this.value.equalsIgnoreCase(that.value);

    }

    @Override
    public int hashCode() {
        int result = this.value.hashCode();
        result = 31 * result + (this.lang != null ? this.lang.hashCode() : 0);
        return result;
    }
}
