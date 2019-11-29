package eu.domibus.api.monitoring.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;


/**
 * DTO abstract class that stores Monitoring Service Info
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public abstract class ServiceInfo implements Serializable {

    /**
     * Service name {@link String}
     */
    protected String name;
    /**
     * Service status {@link MonitoringStatus}
     */
    protected MonitoringStatus status;

    /**
     * Get Monitoring Service name
     *
     * @return Monitoring Service name {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Set Monitoring Service name
     *
     * @param name Monitoring Service name {@link String}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Monitoring Service Status
     *
     * @return Monitoring Service Status {@link MonitoringStatus}
     */
    public MonitoringStatus getStatus() {
        return status;
    }

    /**
     * Set Monitoring Service Status
     *
     * @param status Monitoring Service Status {@link MonitoringStatus}
     */
    public void setStatus(MonitoringStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ServiceInfo that = (ServiceInfo) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(status, that.status)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(status)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("status", status)
                .toString();
    }
}
