package eu.domibus.ext.domain.monitoring;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * DTO class that stores Monitoring Service details
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class MonitoringInfoDTO {

    /**
     * Monitoring Service Info details {@link ServiceInfoDTO}
     */
    protected List<ServiceInfoDTO> services;

    /**
     * Gets Monitoring Service Info details
     *
     * @return Monitoring Service Info details {@link ServiceInfoDTO}
     */
    public List<ServiceInfoDTO> getServices() {
        return services;
    }

    /**
     * Sets Monitoring Service Info details
     *
     * @param services Monitoring Service Info details {@link ServiceInfoDTO}
     */
    public void setServices(List<ServiceInfoDTO> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("services", services)
                .toString();
    }
}
