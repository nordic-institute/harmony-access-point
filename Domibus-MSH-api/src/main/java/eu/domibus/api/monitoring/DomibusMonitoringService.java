package eu.domibus.api.monitoring;

import eu.domibus.api.monitoring.domain.MonitoringInfo;

import java.util.List;

/**
 * All Operations related to monitoring
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public interface DomibusMonitoringService {
    String DB_STATUS_FILTER = "db";

    String JMS_STATUS_FILTER = "jmsBroker";

    String QUARTZ_STATUS_FILTER = "quartzTrigger";

    String dbAccess = "Database";

    String jmsAccess = "JMSBroker";

    String PULL = "pull";

    MonitoringInfo getMonitoringDetails(List<String> filters);

}
