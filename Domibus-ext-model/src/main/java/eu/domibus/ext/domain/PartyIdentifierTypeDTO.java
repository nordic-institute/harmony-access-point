package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Party Identifier Type class for external API
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class PartyIdentifierTypeDTO {

    protected String name;

    protected String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("value", value)
                .toString();
    }
}
