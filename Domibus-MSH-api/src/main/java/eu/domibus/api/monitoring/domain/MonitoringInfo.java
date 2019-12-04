package eu.domibus.api.monitoring.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Stores Monitoring Service details
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class MonitoringInfo {
    /**
     * Monitoring Service Info details {@link ServiceInfo}
     */
    protected List<ServiceInfo> services;

    /**
     * Gets Monitoring Service Info details
     *
     * @return Monitoring Service Info details {@link ServiceInfo}
     */
    public List<ServiceInfo> getServices() {
        return services;
    }

    /**
     * Sets Monitoring Service Info details
     *
     * @param services Monitoring Service Info details {@link ServiceInfo}
     */
    public void setServices(List<ServiceInfo> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("services", services)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MonitoringInfo that = (MonitoringInfo) o;

        return new EqualsBuilder()
                .append(services, that.services)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(services)
                .toHashCode();
    }
}
