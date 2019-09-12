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
}
