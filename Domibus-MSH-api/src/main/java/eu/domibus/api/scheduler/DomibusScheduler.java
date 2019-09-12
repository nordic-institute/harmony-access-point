package eu.domibus.api.scheduler;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Interface for access to the reschedule feature of the Quartz scheduler.
 */
public interface DomibusScheduler {

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

}
