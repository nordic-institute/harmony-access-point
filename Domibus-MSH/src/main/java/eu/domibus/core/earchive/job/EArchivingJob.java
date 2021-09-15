package eu.domibus.core.earchive.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author François Gautier
 * @since 5.0
 */
@DisallowConcurrentExecution
public class EArchivingJob extends DomibusQuartzJobBean {

    @Autowired
    protected EArchiveBatchDispatcherService eArchiveBatchService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        eArchiveBatchService.startBatch();
    }

}
