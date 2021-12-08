package eu.domibus.core.earchive.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@DisallowConcurrentExecution
public class EArchivingCleanupJob extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingCleanupJob.class);

    @Autowired
    protected EArchivingRetentionService eArchivingRetentionService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        LOG.debug("Expire batches");
        eArchivingRetentionService.expireBatches();
        LOG.debug("Start cleaning stored earchive batches");
        eArchivingRetentionService.cleanStoredBatches();
    }

}
