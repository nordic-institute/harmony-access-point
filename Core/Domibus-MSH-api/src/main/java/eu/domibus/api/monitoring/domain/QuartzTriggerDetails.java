package eu.domibus.api.monitoring.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Stores Quartz Trigger Details
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class QuartzTriggerDetails implements Serializable {
    /**
     * Quartz Trigger jobName {@link String}
     */
    protected String jobName;
    /**
     * Quartz Trigger domainName {@link String}
     */
    protected String domainName;
    /**
     * Quartz  trigger status {@link MonitoringStatus}
     */
    protected MonitoringStatus triggerStatus;

    /**
     * Get Quartz Scheduler job Name
     *
     * @return Quartz Scheduler job Name {@link String}
     */
    public String getJobName() {
        return jobName;

    }

    /**
     * Set Quartz Scheduler job Name
     *
     * @param jobName Quartz Scheduler job Name {@link String}
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Get Quartz Scheduler Domain Name
     *
     * @return Quartz Scheduler Domain Name {@link String}
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Set Quartz Scheduler Domain Name
     *
     * @param domainName Quartz Scheduler Domain Name {@link String}
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Get Quartz Trigger State
     *
     * @return Quartz  Trigger State {@link MonitoringStatus}
     */
    public MonitoringStatus getTriggerStatus() {
        return triggerStatus;
    }

    /**
     * Set Quartz Trigger State
     *
     * @param triggerStatus Quartz Trigger State {@link MonitoringStatus}
     */
    public void setTriggerStatus(MonitoringStatus triggerStatus) {
        this.triggerStatus = triggerStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jobName", jobName)
                .append("domainName", domainName)
                .append("triggerStatus", triggerStatus)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        QuartzTriggerDetails that = (QuartzTriggerDetails) o;

        return new EqualsBuilder()
                .append(jobName, that.jobName)
                .append(domainName, that.domainName)
                .append(triggerStatus, that.triggerStatus)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(jobName)
                .append(domainName)
                .append(triggerStatus)
                .toHashCode();
    }
}
