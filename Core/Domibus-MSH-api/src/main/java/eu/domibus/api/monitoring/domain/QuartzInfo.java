package eu.domibus.api.monitoring.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Stores Quartz Monitoring Service details
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class QuartzInfo extends ServiceInfo {

    /**
     * Quartz Trigger details {@link QuartzTriggerDetails}
     */
    protected List<QuartzTriggerDetails> quartzTriggerDetails;

    /**
     * Get List of Quartz Trigger details
     *
     * @return List of Quartz Trigger details {@link QuartzTriggerDetails}
     */
    public List<QuartzTriggerDetails> getQuartzTriggerDetails() {
        return quartzTriggerDetails;
    }

    /**
     * Set List of Quartz Trigger details
     *
     * @param quartzTriggerDetails List of Quartz Trigger details {@link QuartzTriggerDetails}
     */
    public void setQuartzTriggerDetails(List<QuartzTriggerDetails> quartzTriggerDetails) {
        this.quartzTriggerDetails = quartzTriggerDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        QuartzInfo that = (QuartzInfo) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(quartzTriggerDetails, that.quartzTriggerDetails)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(quartzTriggerDetails)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("quartzTriggerDetails", quartzTriggerDetails)
                .append("name", name)
                .append("status", status)
                .toString();
    }
}
