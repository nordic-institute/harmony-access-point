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
 * @since 5.0.7
 * @author Sebastian-Ion TINCU
 */
@DisallowConcurrentExecution
public class EArchivingStuckBatchesJob extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingStuckBatchesJob.class);

    @Autowired
    private EArchivingStuckBatchesService eArchivingStuckBatchesService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        LOG.debug("Marking stuck batches as failed and exporting them again as new batches");
        eArchivingStuckBatchesService.reExportStuckBatches();
    }
}