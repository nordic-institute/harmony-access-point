package eu.domibus.ext.services;


public interface DomibusSchedulerExtService {

    void rescheduleJob(String domainCode, String jobNameToReschedule, String newCronExpression);

    void rescheduleJob(String domainCode, String jobNameToReschedule, Integer newRepeatInterval);
}
