package eu.domibus.ext.domain;

import java.io.Serializable;
import java.util.Objects;

public class QuartzInfoDetailsDTO implements Serializable {

    private String jobName;
    private String domainName;
    private MonitoringStatus triggerStatus;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public MonitoringStatus getTriggerStatus() {
        return triggerStatus;
    }

    public void setTriggerStatus(MonitoringStatus triggerStatus) {
        this.triggerStatus = triggerStatus;
    }

    @Override
    public String toString() {
        return "QuartzInfoDetailsDTO{" +
                "jobName='" + jobName + '\'' +
                ", domainName='" + domainName + '\'' +
                ", triggerStatus=" + triggerStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuartzInfoDetailsDTO that = (QuartzInfoDetailsDTO) o;
        return Objects.equals(jobName, that.jobName) &&
                Objects.equals(domainName, that.domainName) &&
                triggerStatus == that.triggerStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, domainName, triggerStatus);
    }
}
