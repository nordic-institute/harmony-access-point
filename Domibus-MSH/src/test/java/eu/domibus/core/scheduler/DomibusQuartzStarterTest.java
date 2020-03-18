package eu.domibus.core.scheduler;

import eu.domibus.api.property.DomibusConfigurationService;
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

import java.util.*;

/**
 * JUnit for {@link DomibusQuartzStarter}
 *
 * @author Catalin Enache, Soumya Chandran
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
            result = Class.forName("eu.domibus.core.ebms3.sender.retry.SendRetryWorker");

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
            result = new SchedulerException(new ClassNotFoundException("required class was not found: eu.domibus.core.ebms3.sender.retry.SendRetryWorker"));

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
    public void getTriggerInfoMultiTenantAwareTest(@Injectable QuartzTriggerDetails triggerInfo) throws Exception {
        final List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        new Expectations(domibusQuartzStarter) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            domibusQuartzStarter.getGeneralSchedulersInfo(generalSchedulers);
            result = triggerInfoList;
        }};

        domibusQuartzStarter.getTriggerInfo();

        new Verifications() {{
            domibusQuartzStarter.getGeneralSchedulersInfo(generalSchedulers);
            times = 1;
        }};

    }

    @Test
    public void getTriggerInfoNonMultiTenantAwareTest(@Injectable QuartzTriggerDetails triggerInfo) throws Exception {

        generalSchedulers.add(scheduler);
        final List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        new Expectations(domibusQuartzStarter) {{
            domibusQuartzStarter.getSchedulersInfo(schedulers);
            result = triggerInfoList;
            times = 1;
            domibusConfigurationService.isMultiTenantAware();
            result = false;

        }};

        QuartzInfo domibusMonitoringInfo = domibusQuartzStarter.getTriggerInfo();

        Assert.assertNotNull(domibusMonitoringInfo);

    }

    @Test
    public void getGeneralSchedulersInfoTest(@Injectable QuartzTriggerDetails triggerInfo) throws Exception {
        generalSchedulers.add(scheduler);
        final List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();

        new Expectations() {{
            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;
            domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);
            result = triggerInfoList;
        }};

        domibusQuartzStarter.getGeneralSchedulersInfo(generalSchedulers);
        new Verifications() {{
            domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);
            times = 1;
        }};

    }

    @Test
    public void getSchedulersInfoTest(@Injectable Domain domain,
                                      @Injectable QuartzTriggerDetails triggerInfo) throws Exception {
        schedulers.put(domain, scheduler);
        final List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        new Expectations() {{
            scheduler.getJobGroupNames();
            times = 1;
            result = jobGroups;
            domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);
            result = triggerInfoList;
        }};
        domibusQuartzStarter.getSchedulersInfo(schedulers);
        new Verifications() {{
            domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);
            times = 1;
        }};
    }

    @Test
    public void getTriggerDetailsTest(@Injectable Domain domain,
                                      @Injectable QuartzTriggerDetails triggerInfo) throws Exception {
        schedulers.put(domain, scheduler);
        String jobName = "Retry Worker";
        trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "group1")
                .build();
        final List<Trigger> list = new ArrayList<>();
        list.add(trigger);
        List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        new Expectations() {{
            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            times = 1;
            result = jobKeys;
            jobKey1.getName();
            result = jobName;
            scheduler.getTriggersOfJob(jobKey1);
            times = 1;
            result = list;
        }};

        domibusQuartzStarter.getTriggerDetails(scheduler, groupName, domainName);

        new Verifications() {{
            domibusQuartzStarter.getTriggersInErrorOrBlockedState(scheduler, domainName, triggerInfoList, jobName, list);
            times = 1;
        }};
    }

    @Test
    public void getTriggersInErrorOrBlockedStateTest(@Injectable Domain domain,
                                                     @Injectable QuartzTriggerDetails quartzTriggerDetails) throws SchedulerException {
        schedulers.put(domain, scheduler);
        String jobName = "Retry Worker";
        final TriggerKey triggerKey = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "group1")
                .build().getKey();
        final List<Trigger> triggers = new ArrayList<>();
        triggers.add(trigger);
        final Trigger.TriggerState triggerState = Trigger.TriggerState.ERROR;
        List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        new Expectations(domibusQuartzStarter) {{
            trigger.getKey();
            result = triggerKey;

            scheduler.getTriggerState(withAny(triggerKey));
            result = triggerState;

            domibusQuartzStarter.isTriggerInErrorOrBlockedState(withAny(Trigger.TriggerState.ERROR), withAny(trigger));
            result = true;
        }};

        domibusQuartzStarter.getTriggersInErrorOrBlockedState(scheduler, domainName, triggerInfoList, jobName, triggers);
        new FullVerifications() {{
            domibusQuartzStarter.isTriggerInErrorOrBlockedState(withAny(Trigger.TriggerState.ERROR), trigger);
            times = 1;
        }};
    }

    @Test
    public void isTriggerInErrorOrBlockedStateTest(@Injectable Trigger.TriggerState triggerState,
                                                   @Injectable Trigger trigger) {
        new Expectations() {{
            trigger.getPreviousFireTime();
            times = 1;
        }};

        boolean isErrorOrBlockedState = domibusQuartzStarter.isTriggerInErrorOrBlockedState(Trigger.TriggerState.ERROR, trigger);
        Assert.assertTrue(isErrorOrBlockedState);
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

    @Test
    public void checkSchedulerJobsByTriggerGroupTest(@Injectable Scheduler scheduler,
                                                     @Injectable TriggerKey triggerKey,
                                                     @Injectable Trigger trigger,
                                                     @Injectable JobKey jobKey,
                                                     @Mocked JobDetail jobDetail,
                                                     @Injectable Job job) throws Exception {

        String triggerGroup = "retryWorker";
        new Expectations() {{
            scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup));
            result = triggerKey;
            scheduler.getTrigger(triggerKey);
            result = trigger;
            trigger.getJobKey();
            result = jobKey;
            scheduler.getJobDetail(jobKey);
            result = withAny(jobDetail);
            jobDetail.getJobClass();
            result = new SchedulerException();
        }};
        domibusQuartzStarter.checkSchedulerJobsByTriggerGroup(scheduler, triggerGroup);
        new Verifications() {{
            scheduler.deleteJob(jobKey);
            times = 0;
        }};
    }

    @Test
    public void checkSchedulerJobsFromGroupTest(@Injectable Scheduler scheduler,
                                                @Injectable JobKey jobKey,
                                                @Mocked JobDetail jobDetail,
                                                @Injectable Job job) throws Exception {

        final String groupName = "default";
        final String jobName = "retryWorker";
        final String jobGroup = "jobGroup";

        new Expectations() {{
            scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
            result = jobKey;
            jobKey.getName();
            result = jobName;
            jobKey.getGroup();
            result = jobGroup;
            scheduler.getJobDetail(jobKey);
            result = withAny(jobDetail);
            jobDetail.getJobClass();
            result = new SchedulerException();
        }};

        domibusQuartzStarter.checkSchedulerJobsFromGroup(scheduler, groupName);

        new Verifications() {{
            scheduler.deleteJob(jobKey);
            times = 0;
        }};
    }
}