package eu.domibus.core.monitoring;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.monitoring.DomibusMonitoringService;
import eu.domibus.api.monitoring.domain.*;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Get Monitoring Details by checking the DB, JMS Broker and Quarter Trigger based on the filters
 *
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
@Service
public class DomibusMonitoringDefaultService implements DomibusMonitoringService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringDefaultService.class);

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userService;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    protected DomibusScheduler domibusQuartzScheduler;

    @Autowired
    DomainCoreConverter domainCoreConverter;

    @Override
    public MonitoringInfo getMonitoringDetails(List<String> filters) {
        MonitoringInfo monitoringInfo = new MonitoringInfo();
        List<ServiceInfo> services = new ArrayList<>();
        monitoringInfo.setServices(services);
        for (String filter : filters) {
            switch (filter) {
                case DB_STATUS_FILTER:
                    DataBaseInfo dataBaseInfo = getDataBaseDetails();
                    services.add(dataBaseInfo);
                    break;
                case JMS_STATUS_FILTER:
                    JmsBrokerInfo jmsBrokerInfo = getJMSBrokerDetails();
                    services.add(jmsBrokerInfo);
                    break;
                case QUARTZ_STATUS_FILTER:
                    QuartzInfo quartzInfo = getQuartzTriggerDetails();
                    services.add(quartzInfo);
                    break;
                default:
                    dataBaseInfo = getDataBaseDetails();
                    services.add(dataBaseInfo);
                    jmsBrokerInfo = getJMSBrokerDetails();
                    services.add(jmsBrokerInfo);
                    quartzInfo = getQuartzTriggerDetails();
                    services.add(quartzInfo);
                    break;
            }
        }
        return monitoringInfo;
    }

    protected DataBaseInfo getDataBaseDetails() {
        DataBaseInfo monitoringInfo = new DataBaseInfo();
        try {
            userService.findUsers();
            monitoringInfo.setName(dbAccess);
            monitoringInfo.setStatus(MonitoringStatus.NORMAL);
            LOG.debug("Domibus Database in  Normal Status");

        } catch (Exception ex) {
            LOG.error("Error trying to Access Database", ex);
            monitoringInfo.setName(dbAccess);
            monitoringInfo.setStatus(MonitoringStatus.ERROR);
        }
        return monitoringInfo;
    }


    protected JmsBrokerInfo getJMSBrokerDetails() {
        JmsBrokerInfo jmsBrokerInfo = new JmsBrokerInfo();
        try {
            jmsManager.getDestinationSize(PULL);
            jmsBrokerInfo.setName(jmsAccess);
            jmsBrokerInfo.setStatus(MonitoringStatus.NORMAL);
            LOG.debug("Domibus MS Broker in  Normal Status");

        } catch (Exception ex) {
            jmsBrokerInfo.setName(jmsAccess);
            jmsBrokerInfo.setStatus(MonitoringStatus.ERROR);
            LOG.error("Error trying Access JMS Broker", ex);
        }
        return jmsBrokerInfo;
    }


    protected QuartzInfo getQuartzTriggerDetails() {
        QuartzInfo quartzInfo = null;
        try {
            quartzInfo = domibusQuartzScheduler.getTriggerInfo();

        } catch (Exception ex) {
            LOG.error("Error trying Access Quartz Trigger", ex);
        }
        return quartzInfo;
    }


}
