package eu.domibus.core.property.listeners;


import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.scheduler.DomainSchedulerFactoryConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static eu.domibus.core.scheduler.DomainSchedulerFactoryConfiguration.*;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 * <p>
 * Handles the change of archive active property
 */
@Service
public class ArchiveActivePropertyChangeListener implements DomibusPropertyChangeListener {

    protected final DomibusScheduler domibusScheduler;
    protected final DomainService domainService;

    public ArchiveActivePropertyChangeListener(DomainService domainService, DomibusScheduler domibusScheduler) {
        this.domibusScheduler = domibusScheduler;
        this.domainService = domainService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        Domain domain = domainService.getDomain(domainCode);
        if (Boolean.valueOf(propertyValue)) {
            domibusScheduler.resumeJob(domain, EARCHIVE_CONTINUOUS_JOB);
            domibusScheduler.resumeJob(domain,EARCHIVE_SANITIZER_JOB);
            domibusScheduler.resumeJob(domain,EARCHIVE_CLEANUP_JOB);
        } else {
            domibusScheduler.pauseJob(domain,EARCHIVE_CONTINUOUS_JOB);
            domibusScheduler.pauseJob(domain,EARCHIVE_SANITIZER_JOB);
            domibusScheduler.pauseJob(domain,EARCHIVE_CLEANUP_JOB);
        }
    }


}
