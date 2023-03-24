package eu.domibus.core.earchive.job;

import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.message.retention.MessageRetentionService;
import eu.domibus.core.message.retention.RetentionWorker;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@DisallowConcurrentExecution
public class EArchivingContinuousJob extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingContinuousJob.class);

    @Autowired
    protected EArchiveBatchDispatcherService eArchiveBatchService;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        if (!configurationDAO.configurationExists()) {
            LOG.debug("Missing pMode configuration.");
            return;
        }
        eArchiveBatchService.startBatch(domain, EArchiveRequestType.CONTINUOUS);
    }

}
