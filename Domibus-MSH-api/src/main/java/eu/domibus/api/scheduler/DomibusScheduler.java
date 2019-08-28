package eu.domibus.api.scheduler;

import eu.domibus.api.multitenancy.Domain;

public interface DomibusScheduler {

    void rescheduleJob(Domain domain, String jobNameToReschedule, String newCronExpression) throws DomibusSchedulerException;

    void rescheduleJob(Domain domain, String jobNameToReschedule, Integer newRepeatInterval) throws DomibusSchedulerException;

}
