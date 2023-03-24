package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @since 4.2
 * @author Catalin Enache
 */
public class ProcessDTO {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .toString();
    }
}
