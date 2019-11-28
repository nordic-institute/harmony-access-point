package eu.domibus.core.monitoring;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.monitoring.*;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.api.monitoring.DomibusMonitoringService;
import eu.domibus.api.user.User;
import eu.domibus.common.services.UserService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
@Service
public class DomibusMonitoringDefaultService implements DomibusMonitoringService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringDefaultService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    protected DomibusScheduler domibusQuartzScheduler;

    @Autowired
    DomainCoreConverter domainCoreConverter;

    @Override
    public DomibusMonitoringInfo getDomibusStatus(List<String> filters) {
        DomibusMonitoringInfo domibusMonitoringInfo = new DomibusMonitoringInfo();
        List<ServiceInfo> services = new ArrayList<>();
        domibusMonitoringInfo.setServices(services);
        for (String filter : filters) {
            if (filter.equals(DB_STATUS_FILTER)) {
                DataBaseInfo dataBaseInfo = getDataBaseDetails();
                services.add(dataBaseInfo);
            }
            if (filter.equals(JMS_STATUS_FILTER)) {
                JmsBrokerInfo jmsBrokerInfo = getJMSBrokerDetails();
                services.add(jmsBrokerInfo);
            }
            if (filter.equals(QUARTZ_STATUS_FILTER)) {
                QuartzInfo quartzInfo = getQuartzTriggerDetails();
                services.add(quartzInfo);
            }
            if (filter.equals(ALL_STATUS_FILTER)) {
                DataBaseInfo dataBaseInfo = getDataBaseDetails();
                services.add(dataBaseInfo);
                JmsBrokerInfo jmsBrokerInfo = getJMSBrokerDetails();
                services.add(jmsBrokerInfo);
                QuartzInfo quartzInfo = getQuartzTriggerDetails();
                services.add(quartzInfo);
            }
        }
        return domibusMonitoringInfo;
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
