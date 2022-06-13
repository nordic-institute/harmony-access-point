package eu.domibus.ext.domain.monitoring;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class that stores Quartz Trigger Details
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class QuartzTriggerDetailsDTO implements Serializable {

    /**
     * Quartz Trigger jobName {@link String}
     */
    private String jobName;
    /**
     * Quartz Trigger domainName {@link String}
     */
    private String domainName;
    /**
     * Quartz  trigger status {@link MonitoringStatus}
     */
    private MonitoringStatus triggerStatus;

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
}
