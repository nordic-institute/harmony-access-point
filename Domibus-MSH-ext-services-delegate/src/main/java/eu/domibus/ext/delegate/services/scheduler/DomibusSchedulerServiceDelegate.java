package eu.domibus.ext.delegate.services.scheduler;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.api.scheduler.DomibusSchedulerException;
import eu.domibus.ext.services.DomibusSchedulerExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
@Service
public class DomibusSchedulerServiceDelegate implements DomibusSchedulerExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusSchedulerServiceDelegate.class);

    protected final DomibusScheduler domibusScheduler;

    protected final DomainService domainService;

    DomibusSchedulerServiceDelegate(DomibusScheduler domibusScheduler, DomainService domainService) {
        LOG.debug("creating DomibusSchedulerServiceDelegate");
        this.domibusScheduler = domibusScheduler;
        this.domainService = domainService;
    }

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

    @Override
    public void markJobForDeletion(String domainCode, String jobNameToDelete) {
        Domain domain = domainService.getDomain(domainCode);
        domibusScheduler.markJobForDeletionByDomain(domain, jobNameToDelete);
    }

    @Override
    public void markJobForPausing(String domainCode, String jobNameToPause) {
        LOG.debug("markJobForPausing called with domainCode=[{}] and jobNameToPause=[{}] and domainService=[{}] and domibusScheduler=[{}].",
                domainCode, jobNameToPause, domainService, domibusScheduler);
        Domain domain = domainService.getDomain(domainCode);
        domibusScheduler.markJobForPausingByDomain(domain, jobNameToPause);
    }

    @Override
    public void pauseJobs(String domainCode, String... jobNamesToPause) {
        Domain domain = domainService.getDomain(domainCode);
        domibusScheduler.pauseJobs(domain, jobNamesToPause);
    }

    @Override
    public void resumeJobs(String domainCode, String... jobNamesToResume) {
        Domain domain = domainService.getDomain(domainCode);
        domibusScheduler.resumeJobs(domain, jobNamesToResume);
    }
}
