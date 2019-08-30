package eu.domibus.ext.delegate.services.scheduler;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.api.scheduler.DomibusSchedulerException;
import eu.domibus.ext.services.DomibusSchedulerExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
@Service
public class DomibusSchedulerServiceDelegate implements DomibusSchedulerExtService {

    @Autowired
    protected DomibusScheduler domibusScheduler;

    @Autowired
    protected DomainService domainService;

    @Override
    public void rescheduleJob(String domainCode, String jobNameToReschedule, String newCronExpression) throws DomibusSchedulerException {
        Domain domain = domainService.getDomain(domainCode);
        domibusScheduler.rescheduleJob(domain, jobNameToReschedule, newCronExpression);
    }

    @Override
    public void rescheduleJob(String domainCode, String jobNameToReschedule, Integer newRepeatInterval) throws DomibusSchedulerException {
        Domain domain = domainService.getDomain(domainCode);
        domibusScheduler.rescheduleJob(domain, jobNameToReschedule, newRepeatInterval);
    }
}
