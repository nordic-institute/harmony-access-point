package eu.domibus.core.scheduler;

import eu.domibus.api.monitoring.domain.MonitoringStatus;
import eu.domibus.api.monitoring.domain.QuartzInfo;
import eu.domibus.api.monitoring.domain.QuartzTriggerDetails;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

import static eu.domibus.core.scheduler.DomainSchedulerFactoryConfiguration.*;

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
    protected DomainContextProvider domainContextProvider;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    protected Map<Domain, Scheduler> schedulers = new HashMap<>();

    protected List<Scheduler> generalSchedulers = new ArrayList<>();

    protected List<DomibusDomainQuartzJob> jobsToDelete = new ArrayList<>();
    protected List<DomibusDomainQuartzJob> jobsToPause = new ArrayList<>();

    @PostConstruct
    public void initQuartzSchedulers() {
        // General Schedulers
        try {
            startsSchedulers(GROUP_GENERAL);
        } catch (SchedulerException e) {
            LOG.error("Could not initialize the Quartz Scheduler for general schema", e);
        }

        // Domain Schedulers
        final List<Domain> domains = domainService.getDomains();
        initQuartzSchedulers(domains);
    }

    @Override
    public void onDomainAdded(final Domain domain) {
        initQuartzSchedulers(Arrays.asList(domain));
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        removeScheduler(domain);
    }

    private void initQuartzSchedulers(List<Domain> domains) {
        for (Domain domain : domains) {
            try {
                checkJobsAndStartScheduler(domain);
            } catch (SchedulerException e) {
                LOG.error("Could not initialize the Quartz Scheduler for domain [{}]", domain, e);
            }
        }

        removeMarkedForDeletionJobs();
    }

    @PreDestroy
    public void shutdownQuartzSchedulers() {
        LOG.debug("Shutting down Quartz Schedulers");

        // General Schedulers
        for (Scheduler scheduler : generalSchedulers) {
            LOG.info("Shutting down Quartz Scheduler for general -> scheduler [{}]", scheduler);
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException e) {
                LOG.error("Error while shutting down Quartz Scheduler for general schema", e);
            }
        }

        // Domain Schedulers
        for (Map.Entry<Domain, Scheduler> domainSchedulerEntry : schedulers.entrySet()) {
            final Domain domain = domainSchedulerEntry.getKey();
            final Scheduler scheduler = domainSchedulerEntry.getValue();
            LOG.info("Shutting down Quartz Scheduler for domain [{}] -> scheduler [{}]", domain, scheduler);
            try {
                scheduler.shutdown(true);
            } catch (SchedulerException e) {
                LOG.error("Error while shutting down Quartz Scheduler for domain [{}]", domain, e);
            }
        }
    }

    /**
     * entry point method
     *
     * @param domain the domain
     * @throws SchedulerException Quartz scheduler exception
     */
    public void checkJobsAndStartScheduler(Domain domain) throws SchedulerException {
        domainContextProvider.setCurrentDomain(domain);

        Scheduler scheduler = domibusSchedulerFactory.createScheduler(domain);

        //check Quartz scheduler jobs first
        checkSchedulerJobs(scheduler);

        scheduler.start();
        schedulers.put(domain, scheduler);
        LOG.info("Quartz scheduler started for domain [{}]", domain);

        pauseJobsForCurrentDomain();

        domainContextProvider.clearCurrentDomain();
    }

    private void removeScheduler(Domain domain) {
        if (!schedulers.containsKey(domain)) {
            LOG.info("Quartz Scheduler for domain [{}] not found; exiting.", domain);
            return;
        }

        Scheduler scheduler = schedulers.get(domain);
        try {
            //todo just shutdown or also remove all his jobs??
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            LOG.error("Error while shutting down Quartz Scheduler for domain [{}]", domain, e);
        }
        schedulers.remove(domain);
    }

    protected void removeMarkedForDeletionJobs() {
        jobsToDelete.forEach(domibusDomainQuartzJob -> deleteJobByDomain(domibusDomainQuartzJob.getDomain(), domibusDomainQuartzJob.getQuartzJob()));
    }

    protected void pauseJobsForCurrentDomain() throws DomibusSchedulerException {

        Domain domain = domainContextProvider.getCurrentDomainSafely();

        if (!domibusPropertyProvider.getBooleanProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE)) {
            pauseJobs(domain, EARCHIVE_CONTINUOUS_JOB, EARCHIVE_SANITIZER_JOB, EARCHIVE_CLEANUP_JOB);
        }

        String[] jobNamesToPause = jobsToPause.stream()
                .filter(job -> (domain == null && job.getDomain() == null) || (domain != null && domain.equals(job.getDomain())))
                .map(job -> job.getQuartzJob()).toArray(String[]::new);
        pauseJobs(domain, jobNamesToPause);
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
            if (isTriggerInErrorOrBlockedState(triggerState, trigger, domainName)) {
                MonitoringStatus state = triggerState.equals(Trigger.TriggerState.ERROR) ? MonitoringStatus.ERROR : MonitoringStatus.BLOCKED;
                QuartzTriggerDetails quartzTriggerDetails = new QuartzTriggerDetails();
                quartzTriggerDetails.setDomainName(domainName);
                quartzTriggerDetails.setTriggerStatus(state);
                quartzTriggerDetails.setJobName(jobName);
                LOG.debug("Quartz job [{}] is in [{}] state for domain [{}].", jobName, state, domainName);
                triggerInfoList.add(quartzTriggerDetails);
            }
        }
    }

    protected boolean isTriggerInErrorOrBlockedState(Trigger.TriggerState triggerState, Trigger trigger, String domainName) {
        if (triggerState == null) {
            LOG.debug("Trigger state is null. Determined the trigger state as ERROR for domain [{}]", domainName);
            return true;
        }
        if (triggerState.equals(Trigger.TriggerState.ERROR)) {
            LOG.warn("Trigger [{}] is in ERROR state for domain [{}]", trigger, domainName);
            return true;
        }
        //checking triggers in error status or blocked for the duration of more than 5 minutes
        Date now = new Date();
        Date previousFireTime = trigger.getPreviousFireTime();
        if (previousFireTime == null) {
            LOG.debug("Previous fire time is null: could not determine if trigger [{}] is BLOCKED for domain [{}]. Considering the trigger as not BLOCKED", trigger, domainName);
            return false;

        }

        if (triggerState.equals(Trigger.TriggerState.BLOCKED) && (now.getTime() - previousFireTime.getTime() > TRIGGER_BLOCKED_DURATION)) {
            LOG.warn("Trigger [{}] is BLOCKED for domain [{}]", trigger, domainName);
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
        LOG.info("Quartz scheduler started for general schema");
    }

    /**
     * Checks for all the jobs related with trigger group {@code triggerGroup}
     *
     * @param scheduler    Scheduler
     * @param triggerGroup Trigger Group name
     * @throws SchedulerException Quartz scheduler exception
     */
    protected void checkSchedulerJobsByTriggerGroup(Scheduler scheduler, String triggerGroup) throws SchedulerException {
        LOG.info("Start checking Quartz jobs with trigger group [{}]...", triggerGroup);

        for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup))) {
            Trigger trigger = scheduler.getTrigger(triggerKey);
            JobKey jobKey = trigger.getJobKey();

            checkSchedulerJob(scheduler, jobKey);
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
            //go through jobs to see which one throws ClassNotFoundException
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                checkSchedulerJob(scheduler, jobKey);
            }
        }
    }

    /**
     * check scheduler job, and removes it if it throws ClassNotFoundException
     *
     * @param scheduler the scheduler
     * @param jobKey    the job key
     */
    protected void checkSchedulerJob(Scheduler scheduler, JobKey jobKey) {
        LOG.info("Found Quartz job: {} from group: {}", jobKey.getName(), jobKey.getGroup());

        try {
            scheduler.getJobDetail(jobKey).getJobClass().getName();
        } catch (SchedulerException se) {
            if (ExceptionUtils.getRootCause(se) instanceof ClassNotFoundException) {
                deleteSchedulerJob(scheduler, jobKey, se);
            }
        }
    }

    /**
     * remove scheduler job
     *
     * @param scheduler the scheduler
     * @param jobKey    the job key to remove
     * @param se        the exception that triggered the job deletion
     */
    protected void deleteSchedulerJob(Scheduler scheduler, JobKey jobKey, SchedulerException se) {
        // the job deletion needs to happen inside a transaction,
        // ensuring that, when the scheduler is started immediately afterwards, the deletion is already committed
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    scheduler.deleteJob(jobKey);
                    if (se != null) {
                        LOG.info("DELETED Quartz job: {} of scheduler {} from group: {} cause: {}", jobKey.getName(), scheduler.getSchedulerName(), jobKey.getGroup(), se.getMessage());
                    } else {
                        LOG.info("DELETED Quartz job: {} of scheduler {} from group: {}", jobKey.getName(), scheduler.getSchedulerName(), jobKey.getGroup());
                    }
                } catch (Exception e) {
                    LOG.error("Error while deleting Quartz job: {}", jobKey.getName(), e);
                }
            }
        });
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
            LOG.error("Error rescheduling job [{}]", jobNameToReschedule, ex);
            throw new DomibusSchedulerException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(noRollbackFor = DomibusSchedulerException.class)
    public void pauseJob(Domain domain, String jobNameToPause) throws DomibusSchedulerException {
        pauseJobs(domain, jobNameToPause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(noRollbackFor = DomibusSchedulerException.class)
    public void resumeJob(Domain domain, String jobNameToResume) throws DomibusSchedulerException {
        resumeJobs(domain, jobNameToResume);
    }

    @Override
    @Transactional(noRollbackFor = DomibusSchedulerException.class)
    public void pauseJobs(Domain domain, String... jobNamesToPause) throws DomibusSchedulerException {
        LOG.debug("Pause cron jobs [{}]!", Arrays.asList(jobNamesToPause));
        Scheduler scheduler = domain != null ? schedulers.get(domain) : generalSchedulers.get(0);

        for (String jobNameToPause : jobNamesToPause) {
            LOG.debug("Pause cron job [{}]!", jobNameToPause);
            try {
                JobKey jobKey = findJob(scheduler, jobNameToPause);
                if (jobKey == null) {
                    LOG.warn("Can not pause the job [{}] because it does not exist!", jobNameToPause);
                    continue;
                }
                scheduler.pauseJob(jobKey);
            } catch (SchedulerException ex) {
                LOG.error("Error pausing the job [{}]", jobNameToPause, ex);
                throw new DomibusSchedulerException(ex);
            }
        }
    }

    @Override
    @Transactional(noRollbackFor = DomibusSchedulerException.class)
    public void resumeJobs(Domain domain, String... jobNamesToResume) throws DomibusSchedulerException {
        LOG.debug("Resume cron jobs [{}]!", Arrays.asList(jobNamesToResume));
        Scheduler scheduler = domain != null ? schedulers.get(domain) : generalSchedulers.get(0);
        for (String jobNameToResume : jobNamesToResume) {
            try {
                LOG.debug("Resume cron job [{}]!", jobNameToResume);

                JobKey jobKey = findJob(scheduler, jobNameToResume);
                if (jobKey == null) {
                    LOG.warn("Can not resume the job [{}] because it does not exist!", jobNameToResume);
                    continue;
                }
                scheduler.resumeJob(jobKey);
            } catch (SchedulerException ex) {
                LOG.error("Error resuming the job [{}]", jobNameToResume, ex);
                throw new DomibusSchedulerException(ex);
            }
        }
    }

    @Override
    public void markJobForDeletionByDomain(Domain domain, String jobNameToDelete) {
        jobsToDelete.add(new DomibusDomainQuartzJob(domain, jobNameToDelete));
    }

    @Override
    public void markJobForPausingByDomain(Domain domain, String jobName) {
        jobsToPause.add(new DomibusDomainQuartzJob(domain, jobName));
    }

    protected void deleteJobByDomain(Domain domain, String jobNameToDelete) throws DomibusSchedulerException {
        try {
            String domainCode = domain != null ? domain.getCode() : null;
            LOG.debug("Deleting job with jobKey=[{}] for domain=[{}]", jobNameToDelete, domainCode);
            Scheduler scheduler = domain != null ? schedulers.get(domain) : generalSchedulers.get(0);
            if (scheduler != null) {
                JobKey jobKey = findJob(scheduler, jobNameToDelete);
                if (jobKey != null) {
                    deleteSchedulerJob(scheduler, jobKey, null);
                }

            }
        } catch (SchedulerException ex) {
            LOG.error("Error deleting job [{}] ", jobNameToDelete, ex);
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
            LOG.warn("Invalid repeat interval: [{}] for job [{}]", newRepeatInterval, jobNameToReschedule);
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
