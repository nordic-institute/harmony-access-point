package eu.domibus.core.ebms3.receiver.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_FINAL_RECIPIENT_CLEANUP_OLDER_THAN;

@DisallowConcurrentExecution
public class FinalRecipientCleanupJob extends DomibusQuartzJobBean {
    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FinalRecipientCleanupJob.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        Integer numberOfDays = domibusPropertyProvider.getIntegerProperty(DOMIBUS_FINAL_RECIPIENT_CLEANUP_OLDER_THAN);
        if(numberOfDays==null){
            LOG.debug("Job 'final recipient cleanup' will not be executed because the property [{}] is not set", DOMIBUS_FINAL_RECIPIENT_CLEANUP_OLDER_THAN);
            return;
        }
        LOG.debug("Executing job 'Cleanup final recipients older than [{}] days' at [{}]", numberOfDays, LocalDateTime.now(ZoneOffset.UTC));
        pModeProvider.deleteFinalRecipientsOlderThan(numberOfDays);
    }
}
