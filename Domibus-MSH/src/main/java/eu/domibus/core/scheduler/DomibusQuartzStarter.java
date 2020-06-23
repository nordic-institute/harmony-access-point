package eu.domibus.core.scheduler;

import eu.domibus.api.monitoring.domain.MonitoringStatus;
import eu.domibus.api.monitoring.domain.QuartzInfo;
import eu.domibus.api.monitoring.domain.QuartzTriggerDetails;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.api.scheduler.DomibusSchedulerException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * Quartz scheduler starter class which:
 * <p>
 * 1. checks existing jobs - if {@code ClassNotFoundException} is thrown - it deletes the job.
 * It could be the case of FS-PLUGIN which leaves metadata in {@code QRTZ_*} tables
 * <p>
 * 2. starts manually the Quartz scheduler
 *
 * @author Catalin Enache, Soumya Chandran
 * @version 1.0
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 * @since 3.3.2
 */
@Service
public class DomibusQuartzStarter implements DomibusScheduler {

    /**
     * logger
     */
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusQuartzStarter.class);

    /**
     * Used to check if any Quartz Triggers are blocked for more than 5 minutes.
     */
    private static final long TRIGGER_BLOCKED_DURATION = 5 * 60 * 1000L;

    @Autowired
    protected DomibusSchedulerFactory domibusSchedulerFactory;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    protected Map<Domain, Scheduler> schedulers = new HashMap<>();

    protected List<Scheduler> generalSchedulers = new ArrayList<>();

    @PostConstruct
    public void initQuartzSchedulers() {
        // General Schedulers
        try {
            startsSchedulers("general");
        } catch (SchedulerException e) {
            LOG.error("Could not initialize the Quartz Scheduler for general schema", e);
        }

        // Domain Schedulers
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            try {
                checkJobsAndStartScheduler(domain);
            } catch (SchedulerException e) {
                LOG.error("Could not initialize the Quartz Scheduler for domain [{}]", domain, e);
            }
        }
    }

    @PreDestroy
    public void shutdownQuartzSchedulers() {
        LOG.debug("Shutting down Quartz Schedulers");

        // General Schedulers
        for (Scheduler scheduler : generalSchedulers) {
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException e) {
                LOG.error("Error while shutting down Quartz Scheduler for general schema", e);
            }
        }

        // Domain Schedulers
        for (Map.Entry<Domain, Scheduler> domainSchedulerEntry : schedulers.entrySet()) {
            final Domain domain = domainSchedulerEntry.getKey();
            LOG.debug("Shutting down Quartz Scheduler for domain [{}]", domain);
            final Scheduler quartzScheduler = domainSchedulerEntry.getValue();
            try {
                quartzScheduler.shutdown(true);
            } catch (SchedulerException e) {
                LOG.error("Error while shutting down Quartz Scheduler for domain [{}]", domain, e);
            }
        }
    }

    /**
     * entry point method (post-construct)
     *
     * @param domain the domain
     * @throws SchedulerException Quartz scheduler exception
     */
    public void checkJobsAndStartScheduler(Domain domain) throws SchedulerException {
        Scheduler scheduler = domibusSchedulerFactory.createScheduler(domain);

        //check Quartz scheduler jobs first
        checkSchedulerJobs(scheduler);

        scheduler.start();
        schedulers.put(domain, scheduler);
        LOG.info("Quartz scheduler [{}] started for domain [{}]", scheduler, domain);
    }

    /**
     * Get Quartz Trigger Details with jobName, domainName and trigger status.
     *
     * @throws SchedulerException Quartz scheduler exception
     */

    public QuartzInfo getTriggerInfo() throws Exception {
        List<QuartzTriggerDetails> schedulerTriggers = getSchedulersInfo(schedulers);
        if (domibusConfigurationService.isMultiTenantAware()) {
            List<QuartzTriggerDetails> generalSchedulerTriggers = getGeneralSchedulersInfo(generalSchedulers);
            schedulerTriggers.addAll(generalSchedulerTriggers);
        }
        MonitoringStatus state = (schedulerTriggers.size() > 0) ? MonitoringStatus.ERROR : MonitoringStatus.NORMAL;
        QuartzInfo quartzInfo = new QuartzInfo();
        quartzInfo.setStatus(state);
        quartzInfo.setName("Quartz Trigger");
        quartzInfo.setQuartzTriggerDetails(schedulerTriggers);
        LOG.debug(" Quartz Scheduler trigger Info [{}]", quartzInfo);
        return quartzInfo;
    }

    /**
     * Get General Schedulers Info and trigger details
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    protected List<QuartzTriggerDetails> getGeneralSchedulersInfo(List<Scheduler> generalSchedulers) throws SchedulerException {
        List<QuartzTriggerDetails> triggers = new ArrayList<>();
        for (Scheduler scheduler : generalSchedulers) {
            for (String groupName : scheduler.getJobGroupNames()) {
                List<QuartzTriggerDetails> triggerInfoList = getTriggerDetails(scheduler, groupName, null);
                triggers.addAll(triggerInfoList);
            }
        }
        return triggers;
    }

    /**
     * Get Quartz Schedulers Info for all the domains, for both single and  multi tenant scenario
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    protected List<QuartzTriggerDetails> getSchedulersInfo(Map<Domain, Scheduler> schedulers) throws SchedulerException {
        List<QuartzTriggerDetails> triggers = new ArrayList<>();
        for (Map.Entry<Domain, Scheduler> domainSchedulerEntry : schedulers.entrySet()) {
            final Domain domain = domainSchedulerEntry.getKey();
            String domainName = domain.getName();
            Scheduler quartzScheduler = domainSchedulerEntry.getValue();
            LOG.debug("Quartz Scheduler  [{}] for domain [{}]", quartzScheduler, domain);
            for (String groupName : quartzScheduler.getJobGroupNames()) {
                List<QuartzTriggerDetails> triggerInfoList = getTriggerDetails(quartzScheduler, groupName, domainName);
                triggers.addAll(triggerInfoList);
            }

        }

        return triggers;
    }

    /**
     * Get Quartz Trigger Details with Job Name and Trigger State, for both single and  multi tenant scenario
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    protected List<QuartzTriggerDetails> getTriggerDetails(Scheduler quartzScheduler, String groupName, String domainName) throws SchedulerException {
        List<QuartzTriggerDetails> triggerInfoList = new ArrayList<>();
        for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
            String jobName = jobKey.getName();
            List<Trigger> triggers = (List<Trigger>) quartzScheduler.getTriggersOfJob(jobKey);
            if (CollectionUtils.isNotEmpty(triggers)) {
                getTriggersInErrorOrBlockedState(quartzScheduler, domainName, triggerInfoList, jobName, triggers);
            }
        }
        return triggerInfoList;
    }

    protected void getTriggersInErrorOrBlockedState(Scheduler quartzScheduler, String domainName, List<QuartzTriggerDetails> triggerInfoList, String jobName, List<Trigger> triggers) throws SchedulerException {
        for (Trigger trigger : triggers) {
            Trigger.TriggerState triggerState = quartzScheduler.getTriggerState(trigger.getKey());
            if (isTriggerInErrorOrBlockedState(triggerState, trigger)) {
                MonitoringStatus state = triggerState.equals(Trigger.TriggerState.ERROR) ? MonitoringStatus.ERROR : MonitoringStatus.BLOCKED;
                QuartzTriggerDetails quartzTriggerDetails = new QuartzTriggerDetails();
                quartzTriggerDetails.setDomainName(domainName);
                quartzTriggerDetails.setTriggerStatus(state);
                quartzTriggerDetails.setJobName(jobName);
                LOG.debug("Quartz job [{}] is in [{}] state.", jobName, state);
                triggerInfoList.add(quartzTriggerDetails);
            }
        }
    }

    protected boolean isTriggerInErrorOrBlockedState(Trigger.TriggerState triggerState, Trigger trigger) {
        Date now = new Date();
        Date previousFireTime = trigger.getPreviousFireTime();
        //checking triggers in error status or blocked for the duration of more than 5 minutes
        if (triggerState.equals(Trigger.TriggerState.ERROR) ||
                (triggerState.equals(Trigger.TriggerState.BLOCKED) && (now.getTime() - previousFireTime.getTime() > TRIGGER_BLOCKED_DURATION))) {
            return true;
        }
        return false;
    }

    /**
     * Starts scheduler with trigger group equals to {@code triggerGroup}, only if in multi tenant scenario
     *
     * @throws SchedulerException Quartz scheduler exception
     */
    private void startsSchedulers(String triggerGroup) throws SchedulerException {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            return;
        }
        Scheduler generalScheduler = domibusSchedulerFactory.createScheduler(null);

        //check Quartz scheduler jobs first
        checkSchedulerJobsByTriggerGroup(generalScheduler, triggerGroup);

        generalScheduler.start();
        generalSchedulers.add(generalScheduler);
        LOG.info("Quartz scheduler [{}] started for general schema", generalScheduler);
    }

    /**
     * Checks for all the jobs related with trigger group {@code triggerGroup}
     *
     * @param scheduler    Scheduler
     * @param triggerGroup Trigger Group name
     * @throws SchedulerException Quartz scheduler exception
     */
    protected void checkSchedulerJobsByTriggerGroup(Scheduler scheduler, String triggerGroup) throws SchedulerException {
        LOG.info("Start Quartz jobs with trigger group [{}]...", triggerGroup);

        for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup))) {
            Trigger trigger = scheduler.getTrigger(triggerKey);
            JobKey jobKey = trigger.getJobKey();

            try {
                scheduler.getJobDetail(jobKey).getJobClass().getName();
            } catch (SchedulerException se) {
                if (ExceptionUtils.getRootCause(se) instanceof ClassNotFoundException) {
                    try {
                        scheduler.deleteJob(jobKey);
                        LOG.warn("DELETED Quartz job: {} from group: {} cause: {}", jobKey.getName(), jobKey.getGroup(), se.getMessage());
                    } catch (Exception e) {
                        LOG.error("Error while deleting Quartz job: {}", jobKey.getName(), e);
                    }
                }
            }
        }
    }

    /**
     * goes through scheduler jobs and check for {@code ClassNotFoundException}
     *
     * @param scheduler the scheduler
     * @throws SchedulerException Quartz scheduler exception
     */
    protected void checkSchedulerJobs(Scheduler scheduler) throws SchedulerException {
        LOG.info("Start checking Quartz jobs for scheduler [{}]", scheduler.getSchedulerName());

        for (String groupName : scheduler.getJobGroupNames()) {
            checkSchedulerJobsFromGroup(scheduler, groupName);
        }
    }

    /**
     * check scheduler jobs from a given group
     *
     * @param groupName scheduler group name
     * @throws SchedulerException scheduler exception
     */
    protected void checkSchedulerJobsFromGroup(Scheduler scheduler, final String groupName) throws SchedulerException {

        //go through jobs to see which one throws ClassNotFoundException
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

            final String jobName = jobKey.getName();
            final String jobGroup = jobKey.getGroup();

            LOG.info("Found Quartz job: {} from group: {}", jobName, jobGroup);

            try {
                scheduler.getJobDetail(jobKey).getJobClass().getName();
            } catch (SchedulerException se) {
                if (ExceptionUtils.getRootCause(se) instanceof ClassNotFoundException) {
                    try {
                        scheduler.deleteJob(jobKey);
                        LOG.warn("DELETED Quartz job: {} from group: {} cause: {}", jobName, jobGroup, se.getMessage());
                    } catch (Exception e) {
                        LOG.error("Error while deleting Quartz job: {}", jobName, e);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(noRollbackFor = DomibusSchedulerException.class)
    public void rescheduleJob(Domain domain, String jobNameToReschedule, String newCronExpression) throws DomibusSchedulerException {
        try {
            LOG.debug("Rescheduling job [{}] with cron expression: [{}]", jobNameToReschedule, newCronExpression);
            Scheduler scheduler = domain != null ? schedulers.get(domain) : generalSchedulers.get(0);
            JobKey jobKey = findJob(scheduler, jobNameToReschedule);
            rescheduleJob(scheduler, jobKey, newCronExpression);
        } catch (SchedulerException ex) {
            LOG.error("Error rescheduling job [{}] ", jobNameToReschedule, ex);
            throw new DomibusSchedulerException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(noRollbackFor = DomibusSchedulerException.class)
    public void rescheduleJob(Domain domain, String jobNameToReschedule, Integer newRepeatInterval) throws DomibusSchedulerException {
        if (newRepeatInterval <= 0) {
            LOG.warn("Invalid repeat interval: [{}]", newRepeatInterval);
            throw new DomibusSchedulerException("Invalid repeat interval: " + newRepeatInterval);
        }
        try {
            LOG.debug("Rescheduling job [{}] with repeat interval: [{}]", jobNameToReschedule, newRepeatInterval);
            Scheduler scheduler = domain != null ? schedulers.get(domain) : generalSchedulers.get(0);
            JobKey jobKey = findJob(scheduler, jobNameToReschedule);
            rescheduleJob(scheduler, jobKey, newRepeatInterval);
        } catch (SchedulerException ex) {
            LOG.error("Error rescheduling job [{}] ", jobNameToReschedule, ex);
            throw new DomibusSchedulerException(ex);
        }
    }

    protected JobKey findJob(Scheduler scheduler, String jobNameToFind) throws SchedulerException {
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                final String jobName = jobKey.getName();
                final String jobGroup = jobKey.getGroup();
                if (StringUtils.equalsIgnoreCase(jobName, jobNameToFind)) {
                    LOG.debug("Job [{}] found in group [{}]", jobName, jobGroup);
                    return jobKey;
                }
            }
        }
        LOG.debug("Job [{}] not found in [{}] scheduler", jobNameToFind, scheduler.getSchedulerName());
        return null;
    }

    protected void rescheduleJob(Scheduler scheduler, JobKey jobKey, String cronExpression) throws SchedulerException {
        Trigger oldTrigger = getTrigger(scheduler, jobKey);
        String triggerName = oldTrigger == null ? null : oldTrigger.getKey().getName();
        Trigger newTrigger = initCronTrigger(scheduler.getJobDetail(jobKey), triggerName, cronExpression);
        rescheduleTrigger(scheduler, oldTrigger, newTrigger);
    }

    protected void rescheduleJob(Scheduler scheduler, JobKey jobKey, Integer repeatInterval) throws SchedulerException {
        Trigger oldTrigger = getTrigger(scheduler, jobKey);
        String triggerName = oldTrigger == null ? null : oldTrigger.getKey().getName();
        Trigger newTrigger = initSimpleTrigger(scheduler.getJobDetail(jobKey), triggerName, repeatInterval);
        rescheduleTrigger(scheduler, oldTrigger, newTrigger);
    }

    private Trigger getTrigger(Scheduler scheduler, JobKey jobKey) throws SchedulerException {
        return scheduler.getTriggersOfJob(jobKey).stream().findFirst().orElse(null);
    }

    private void rescheduleTrigger(Scheduler scheduler, Trigger oldTrigger, Trigger newTrigger) throws SchedulerException {
        if (oldTrigger == null) {
            scheduler.scheduleJob(newTrigger);
        } else {
            scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
        }
    }

    /**
     * Initialize a new cron trigger for a given job detail
     */
    private CronTrigger initCronTrigger(JobDetail jobDetail, String triggerName, String cronExpression) {
        triggerName = triggerName == null ? jobDetail.getKey().getName() + "CronTrigger" : triggerName;
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerName)
                .forJob(jobDetail)
                .usingJobData(jobDetail.getJobDataMap())
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

    /**
     * Initialize a new simple trigger for a given job detail
     */
    private SimpleTrigger initSimpleTrigger(JobDetail jobDetail, String triggerName, Integer repeatInterval) {
        triggerName = triggerName == null ? jobDetail.getKey().getName() + "SimpleTrigger" : triggerName;
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerName)
                .forJob(jobDetail)
                .usingJobData(jobDetail.getJobDataMap())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(repeatInterval).repeatForever())
                .build();
    }

}