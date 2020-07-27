package eu.domibus.core.monitoring;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.monitoring.domain.*;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.user.UserService;
import eu.domibus.core.converter.DomainCoreConverter;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusMonitoringDefaultServiceTest {
    @Tested
    DomibusMonitoringDefaultService domibusMonitoringDefaultService;

    @Injectable
    UserService userManagementService;

    @Injectable
    JMSManager jmsManager;

    @Injectable
    DomibusScheduler domibusQuartzScheduler;

    @Injectable
    DomainCoreConverter domainCoreConverter;

    private static final String DB_STATUS_FILTER = "db";

    private static final String JMS_STATUS_FILTER = "jmsBroker";

    private static final String QUARTZ_STATUS_FILTER = "quartzTrigger";

    private static final String ALL_STATUS_FILTER = "all";

    @Test
    public void getDomibusStatusDBTest() {
        DataBaseInfo dataBaseInfo = new DataBaseInfo();
        dataBaseInfo.setName(DB_STATUS_FILTER);
        dataBaseInfo.setStatus(MonitoringStatus.NORMAL);
        List<String> filter = new ArrayList<>();
        filter.add(DB_STATUS_FILTER);
        new Expectations() {{
            userManagementService.findUsers();
            times =1;
        }};
       MonitoringInfo monitoringInfo = domibusMonitoringDefaultService.getMonitoringDetails(filter);

        Assert.assertNotNull(monitoringInfo);
    }

    @Test
    public void getDomibusStatusJMSBrokerTest(){
        JmsBrokerInfo jmsBrokerInfo = new JmsBrokerInfo();
        jmsBrokerInfo.setName(JMS_STATUS_FILTER);
        jmsBrokerInfo.setStatus(MonitoringStatus.NORMAL);
        List<String> filter = new ArrayList<>();
        filter.add(JMS_STATUS_FILTER);
        new Expectations() {{
            jmsManager.getDestinationSize("pull");
            result = 5;
        }};

        MonitoringInfo monitoringInfo = domibusMonitoringDefaultService.getMonitoringDetails(filter);

        Assert.assertNotNull(monitoringInfo);
    }

    @Test
    public void getDomibusStatusQuartzTriggerTest() throws Exception {
        final String QUARTZ_STATUS_FILTER = "quartzTrigger";
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList);
        List<String> filter = new ArrayList<>();
        filter.add(QUARTZ_STATUS_FILTER);
        new Expectations() {{
         domibusQuartzScheduler.getTriggerInfo();
            result = quartzInfo;
        }};

        MonitoringInfo monitoringInfo = domibusMonitoringDefaultService.getMonitoringDetails(filter);

        Assert.assertNotNull(monitoringInfo);
    }

    @Test
    public void getDomibusStatusAllTest() throws Exception {
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList);
        quartzInfo.setName(QUARTZ_STATUS_FILTER);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList);
        List<String> filter = new ArrayList<>();
        filter.add(ALL_STATUS_FILTER);
        new Expectations() {{
            userManagementService.findUsers();
            times =1;
            jmsManager.getDestinationSize("pull");
            result = 5;
            domibusQuartzScheduler.getTriggerInfo();
            result = quartzInfo;
        }};

        MonitoringInfo monitoringInfo = domibusMonitoringDefaultService.getMonitoringDetails(filter);

        Assert.assertNotNull(monitoringInfo);
    }
    @Test
    public void getDataBaseDetailsTest() {
        new Expectations(domibusMonitoringDefaultService) {{
            userManagementService.findUsers();
            times =1;
        }};

        DataBaseInfo dataBaseInfo = domibusMonitoringDefaultService.getDataBaseDetails();

        Assert.assertNotNull(dataBaseInfo);
    }

    @Test
    public void getJMSBrokerDetailsTest() {

        new Expectations() {{
            jmsManager.getDestinationSize("pull");
            result = 5;
        }};

        JmsBrokerInfo jmsBrokerInfo = domibusMonitoringDefaultService.getJMSBrokerDetails();

        Assert.assertNotNull(jmsBrokerInfo);
    }

    @Test
    public void getQuartzDetailsTest() throws Exception {
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        quartzInfo.setName("Quartz Trigger");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList);
        new Expectations() {{
            domibusQuartzScheduler.getTriggerInfo();
            result = quartzInfo;
        }};

        QuartzInfo quartzInfoResult = domibusMonitoringDefaultService.getQuartzTriggerDetails();

        Assert.assertNotNull(quartzInfoResult);
    }
}
