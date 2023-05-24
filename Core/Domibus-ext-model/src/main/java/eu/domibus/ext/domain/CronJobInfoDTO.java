package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class CronJobInfoDTO {

    protected String name;

    public CronJobInfoDTO() {
    }

    public CronJobInfoDTO( String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CronJobInfoDTO job = (CronJobInfoDTO) o;

        return new EqualsBuilder()
                .append(name, job.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
