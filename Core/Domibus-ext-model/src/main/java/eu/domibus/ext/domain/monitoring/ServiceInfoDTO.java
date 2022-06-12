package eu.domibus.ext.domain.monitoring;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO abstract class that stores Monitoring Service Info
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public abstract class ServiceInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("status", status)
                .toString();
    }
}
