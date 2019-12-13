package eu.domibus.api.monitoring.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Stores JmsBroker Monitoring Service Details
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class JmsBrokerInfo extends ServiceInfo {

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("status", status)
                .toString();
    }
}
