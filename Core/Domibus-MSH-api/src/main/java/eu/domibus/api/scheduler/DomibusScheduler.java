package eu.domibus.api.scheduler;

import eu.domibus.api.monitoring.domain.QuartzInfo;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainsAware;

/**
 * @author Ion Perpegel , soumya chandran
 * @since 4.1.1
 *
 * Interface for access to the reschedule feature of the Quartz scheduler.
 */
public interface DomibusScheduler extends DomainsAware {

    /**
     * Reschedules an existing job with a new cron expression
     * @param domain the domain of the job to be rescheduled
     * @param jobNameToReschedule the name of the job to be rescheduled
     * @param newCronExpression the new cron expression
     */
    void rescheduleJob(Domain domain, String jobNameToReschedule, String newCronExpression) throws DomibusSchedulerException;

    /**
     * Reschedules an existing job with a new cron expression
     * @param domain the domain of the job to be rescheduled
     * @param jobNameToReschedule the name of the job to be rescheduled
     * @param newRepeatInterval the new repeat interval
     */
    void rescheduleJob(Domain domain, String jobNameToReschedule, Integer newRepeatInterval) throws DomibusSchedulerException;

    /**
     * If the job exists and it is not already paused, method pauses the job.
     * @param domain the domain of the job to pause
     * @param jobNameToPause the name of the job to pause
     */
    void pauseJob(Domain domain, String jobNameToPause) throws DomibusSchedulerException;

    /**
     * If the job exists and it is not already paused, method pauses the job(s).
     * @param domain the domain of the jobs to pause
     * @param jobNamesToPause array of the names of jobs to pause
     */
    void pauseJobs(Domain domain, String ... jobNamesToPause) throws DomibusSchedulerException;

    /**
     * If the job exists and it is paused, method resumes the job.
     * @param domain the domain of the job to resume
     * @param jobNameToResume the name of the job to resume
     */
    void resumeJob(Domain domain, String jobNameToResume) throws DomibusSchedulerException;

    /**
     * If the job exists and it is paused, method resumes the job(s).
     * @param domain the domain of the jobs to resume
     * @param jobNamesToResume the names of jobs to resume
     */
    void resumeJobs(Domain domain, String ... jobNamesToResume) throws DomibusSchedulerException;


    /**
     * Get The Quartz Trigger Details
     * @return QuartzInfo with all Job Name, Domain Name and trigger State
     * @throws Exception
     */
    QuartzInfo getTriggerInfo() throws Exception;

    /**
     * Deletes job by domain, jobName
     * @param domain
     * @param jobNameToDelete
     */
    void markJobForDeletionByDomain(Domain domain, String jobNameToDelete);

    /**
     * Marks a job to be paused when possible (after the quartz scheduler is started)
     * @param domain the domain of the job to be paused
     * @param jobName the name of the job to be paused
     */
    void markJobForPausingByDomain(Domain domain, String jobName);
}
