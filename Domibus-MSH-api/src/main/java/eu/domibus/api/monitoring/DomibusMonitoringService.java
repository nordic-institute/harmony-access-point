package eu.domibus.api.monitoring;

import java.util.List;
/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public interface DomibusMonitoringService {
      String DB_STATUS_FILTER = "db";

      String JMS_STATUS_FILTER = "jmsBroker";

      String QUARTZ_STATUS_FILTER = "quartzTrigger";

      String ALL_STATUS_FILTER = "all";

      String dbAccess = "Database";

      String jmsAccess = "JMSBroker";

      String PULL = "pull";

    DomibusMonitoringInfo getDomibusStatus(List<String> filters);

}
