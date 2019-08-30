package eu.domibus.api.scheduler;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Interface for access to the reschedule feature of the Quartz scheduler.
 */
public interface DomibusScheduler {

    void rescheduleJob(Domain domain, String jobNameToReschedule, String newCronExpression) throws DomibusSchedulerException;

    void rescheduleJob(Domain domain, String jobNameToReschedule, Integer newRepeatInterval) throws DomibusSchedulerException;

}
