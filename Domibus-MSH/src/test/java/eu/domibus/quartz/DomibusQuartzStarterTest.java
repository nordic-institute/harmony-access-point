package eu.domibus.quartz;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.monitoring.QuartzInfo;
import eu.domibus.api.monitoring.QuartzInfoDetails;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.util.*;

/**
 * JUnit for {@link DomibusQuartzStarter}
 *
 * @author Catalin Enache
 * @version 1.0
 * @since 3.3.2
 */
@RunWith(JMockit.class)
public class DomibusQuartzStarterTest {

    private final String groupName = "DEFAULT";
    private final String domainName = "domain1";
    private final List<String> jobGroups = Collections.singletonList(groupName);
    private final Set<JobKey> jobKeys = new HashSet<>();
    private final JobKey jobKey1 = new JobKey("retryWorkerJob", groupName);
    private final List<Scheduler> generalSchedulers = new ArrayList<>();
    private final  Map<Domain, Scheduler> schedulers = new HashMap<>();
    @Tested
    private DomibusQuartzStarter domibusQuartzStarter;

    @Injectable
    protected DomibusSchedulerFactory domibusSchedulerFactory;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    protected Scheduler scheduler;



    @Before
    public void setUp() throws Exception {
        jobKeys.add(jobKey1);
    }

    @Test
    public void checkSchedulerJobs_ValidConfig_NoJobDeleted(final @Mocked JobDetailImpl jobDetail) throws Exception {

        new Expectations() {{
            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;

            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            times = 1;
            result = jobKeys;

            scheduler.getJobDetail(jobKey1);
            times = 1;
            result = jobDetail;

            jobDetail.getJobClass();
            times = 1;
            result = Class.forName("eu.domibus.ebms3.sender.SendRetryWorker");

            scheduler.getSchedulerName();
            times = 1;

        }};

        //tested method
        domibusQuartzStarter.checkSchedulerJobs(scheduler);

        new FullVerifications() {{
        }};
    }

    @Test
    public void checkSchedulerJobs_InvalidConfig_JobDeleted(final @Mocked JobDetailImpl jobDetail) throws Exception {

        new Expectations() {{
            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;

            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            times = 1;
            result = jobKeys;

            scheduler.getJobDetail(jobKey1);
            times = 1;
            result = jobDetail;

            jobDetail.getJobClass();
            times = 1;
            result = new SchedulerException(new ClassNotFoundException("required class was not found: eu.domibus.ebms3.sender.SendRetryWorker"));

            scheduler.getSchedulerName();
            times = 1;
        }};

        //tested method
        domibusQuartzStarter.checkSchedulerJobs(scheduler);

        new FullVerifications() {{
            JobKey jobKeyActual;
            scheduler.deleteJob(jobKeyActual = withCapture());
            times = 1;
            Assert.assertEquals(jobKey1, jobKeyActual);
        }};
    }

    @Test
    public void getTriggerInfoMultiTenantAwareTest() throws Exception {
        generalSchedulers.add(scheduler);
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzInfoDetails> triggerInfoList = new ArrayList<>();
        QuartzInfoDetails triggerInfo = new QuartzInfoDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzInfoDetails(triggerInfoList);
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result=true;
            domibusQuartzStarter.getGeneralSchedulersInfo(generalSchedulers, triggerInfoList);
            times = 1;
           /* result=jobKey1;
            scheduler.getTriggersOfJob(jobKey1);
            result = quartzInfo;*/
        }};

        QuartzInfo domibusMonitoringInfo = domibusQuartzStarter.getTriggerInfo();

        Assert.assertNotNull(domibusMonitoringInfo);

    }

    @Test
    public void getTriggerInfoNonMultiTenantAwareTest() throws Exception {

        generalSchedulers.add(scheduler);
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzInfoDetails> triggerInfoList = new ArrayList<>();
        QuartzInfoDetails triggerInfo = new QuartzInfoDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzInfoDetails(triggerInfoList);
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result=false;
            domibusQuartzStarter.getSchedulersInfo(schedulers, triggerInfoList);
            times = 1;
            //result=jobKey1;
           /* scheduler.getTriggersOfJob(jobKey1);
            result = quartzInfo;*/
        }};

        QuartzInfo domibusMonitoringInfo = domibusQuartzStarter.getTriggerInfo();

        Assert.assertNotNull(domibusMonitoringInfo);

    }

    @Test
    public void getGeneralSchedulersInfoTest() throws Exception {
        generalSchedulers.add(scheduler);
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzInfoDetails> triggerInfoList = new ArrayList<>();
        QuartzInfoDetails triggerInfo = new QuartzInfoDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzInfoDetails(triggerInfoList);
        new Expectations() {{

            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;
            domibusQuartzStarter.getTriggerDetails(scheduler, triggerInfoList, groupName, domainName);
            times = 1;
        }};

        domibusQuartzStarter.getGeneralSchedulersInfo(generalSchedulers, triggerInfoList);
    }

    @Test
    public void getSchedulersInfoTest() throws Exception {
        schedulers.put(new Domain(), scheduler);
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzInfoDetails> triggerInfoList = new ArrayList<>();
        QuartzInfoDetails triggerInfo = new QuartzInfoDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzInfoDetails(triggerInfoList);
        new Expectations() {{

            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;
            domibusQuartzStarter.getTriggerDetails(scheduler, triggerInfoList, groupName, domainName);
            times = 1;
        }};

        domibusQuartzStarter.getSchedulersInfo(schedulers, triggerInfoList);
    }

    @Test
    public void getTriggerDetailsTest() throws Exception {
        schedulers.put(new Domain(), scheduler);

        TriggerKey triggerKey = new TriggerKey("trigger-key-name", "trigger-key-name");
        String triggerState = "ERROR";
        Trigger trigger = new SimpleTriggerImpl();
        final List<Trigger> list = new ArrayList<>();
        list.add(trigger);
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzInfoDetails> triggerInfoList = new ArrayList<>();
        QuartzInfoDetails triggerInfo = new QuartzInfoDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzInfoDetails(triggerInfoList);

        new Expectations() {{

            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            times = 1;
            result = jobKeys;

            scheduler.getTriggersOfJob(jobKey1);
            times = 1;
            result = any;
            result=triggerKey;
            /*scheduler.getTriggerState(triggerKey);
            times = 1;*/
            result = triggerState;
        }};

        domibusQuartzStarter.getTriggerDetails(scheduler, triggerInfoList,groupName, domainName);
    }
}