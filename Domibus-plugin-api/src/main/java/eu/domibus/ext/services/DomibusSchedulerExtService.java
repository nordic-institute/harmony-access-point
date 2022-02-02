package eu.domibus.ext.services;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Service used for operations related to the quartz scheduler:
 *
 * <ul>
 *     <li>Reschedule a domain-specific job with a new cron expression</li>
 *     <li>Reschedule a domain-specific job with a new repeat interval</li>
 * </ul>
 */
public interface DomibusSchedulerExtService {

    /**
     * Reschedules an existing job with a new cron expression
     * @param domainCode the domain of the job to be rescheduled
     * @param jobNameToReschedule the name of the job to be rescheduled
     * @param newCronExpression the new cron expression
     */
    void rescheduleJob(String domainCode, String jobNameToReschedule, String newCronExpression);

    /**
     * Reschedules an existing job with a new repeat interval
     * @param domainCode the domain of the job to be rescheduled
     * @param jobNameToReschedule the name of the job to be rescheduled
     * @param newRepeatInterval the new repeat interval
     */
    void rescheduleJob(String domainCode, String jobNameToReschedule, Integer newRepeatInterval);

    /**
     * Marks a Quartz jobs for deletion (once all domains schedulers where initialised)
     * @param domainCode
     * @param jobNameToDelete
     */
    void markJobForDeletion(String domainCode, String jobNameToDelete);

    /**
     * Marks a Quartz jobs for pausing (once all domains schedulers where initialised)
     * @param domainCode
     * @param jobName
     */
    void markJobForPausing(String domainCode, String jobName);

    /**
     * Pauses existing jobs
     * @param domainCode the domain of the job(s) to be paused
     * @param jobNamesToPause the names of the job(s) to be paused
     */
    void pauseJobs(String domainCode, String... jobNamesToPause);

    /**
     * Resumes existing jobs
     * @param domainCode the domain of the job(s) to be resumed
     * @param jobNamesToResume the names of the job(s) to be resumed
     */
    void resumeJobs(String domainCode, String... jobNamesToResume);
}
