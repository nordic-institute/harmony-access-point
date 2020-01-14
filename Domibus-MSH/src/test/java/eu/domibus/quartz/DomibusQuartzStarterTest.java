package eu.domibus.quartz;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.monitoring.domain.QuartzInfo;
import eu.domibus.api.monitoring.domain.QuartzTriggerDetails;
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
    private final Map<Domain, Scheduler> schedulers = new HashMap<>();
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

    @Injectable
    protected Trigger trigger;


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
        final List<QuartzTriggerDetails>[] triggerInfoList = new List[]{new ArrayList<>()};
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList[0].add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList[0]);
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            triggerInfoList[0] = domibusQuartzStarter.getGeneralSchedulersInfo(generalSchedulers);
            times = 1;
        }};

        QuartzInfo domibusMonitoringInfo = domibusQuartzStarter.getTriggerInfo();

        Assert.assertNotNull(domibusMonitoringInfo);

    }

    @Test
    public void getTriggerInfoNonMultiTenantAwareTest() throws Exception {

        generalSchedulers.add(scheduler);
        QuartzInfo quartzInfo = new QuartzInfo();
        final List<QuartzTriggerDetails>[] triggerInfoList = new List[]{new ArrayList<>()};
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList[0].add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList[0]);
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
            triggerInfoList[0] = domibusQuartzStarter.getSchedulersInfo(schedulers);
            times = 1;
        }};

        QuartzInfo domibusMonitoringInfo = domibusQuartzStarter.getTriggerInfo();

        Assert.assertNotNull(domibusMonitoringInfo);

    }

    @Test
    public void getGeneralSchedulersInfoTest() throws Exception {
        generalSchedulers.add(scheduler);
        QuartzInfo quartzInfo = new QuartzInfo();
        final List<QuartzTriggerDetails>[] triggerInfoList = new List[]{new ArrayList<>()};
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList[0].add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList[0]);
        new Expectations() {{

            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;
            triggerInfoList[0] = domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);
            times = 1;
        }};

        triggerInfoList[0] = domibusQuartzStarter.getGeneralSchedulersInfo(generalSchedulers);
    }

    @Test
    public void getSchedulersInfoTest() throws Exception {
        schedulers.put(new Domain(), scheduler);
        QuartzInfo quartzInfo = new QuartzInfo();
        final List<QuartzTriggerDetails>[] triggerInfoList = new List[]{new ArrayList<>()};
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList[0].add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList[0]);
        new Expectations() {{

            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;
            triggerInfoList[0] = domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);
            times = 1;
        }};

        triggerInfoList[0] = domibusQuartzStarter.getSchedulersInfo(schedulers);
    }

    @Test
    public void getTriggerDetailsTest() throws Exception {
        schedulers.put(new Domain(), scheduler);
        trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "group1")
                .build();
        final List<Trigger> list = new ArrayList<>();
        list.add(trigger);
        QuartzInfo quartzInfo = new QuartzInfo();
        List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        QuartzTriggerDetails triggerInfo = new QuartzTriggerDetails();
        triggerInfo.setJobName("Retry Worker");
        triggerInfoList.add(triggerInfo);
        quartzInfo.setQuartzTriggerDetails(triggerInfoList);

        new Expectations() {{

            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            times = 1;
            result = jobKeys;

            scheduler.getTriggerState(trigger.getKey());
            result = Trigger.TriggerState.ERROR;

            scheduler.getTriggersOfJob(jobKey1);
            times = 1;
            result = list;

        }};

        domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);
    }

    @Test
    public void checkJobsAndStartSchedulerTest(@Injectable Domain domain,
                                               @Injectable Scheduler scheduler) throws Exception {
        new Expectations() {{
            domibusSchedulerFactory.createScheduler(domain);
            result = scheduler;
            domibusQuartzStarter.checkSchedulerJobs(scheduler);
            times = 1;
        }};

        domibusQuartzStarter.checkJobsAndStartScheduler(domain);
        new Verifications() {{
            scheduler.start();
            times = 1;
        }};
    }
}