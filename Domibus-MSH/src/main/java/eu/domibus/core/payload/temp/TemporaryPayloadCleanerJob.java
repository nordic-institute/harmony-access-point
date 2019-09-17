package eu.domibus.core.payload.temp;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.GeneralQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@DisallowConcurrentExecution
public class TemporaryPayloadCleanerJob extends GeneralQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TemporaryPayloadCleanerJob.class);

    @Autowired
    protected TemporaryPayloadService temporaryPayloadService;

    @Override
    protected void executeJob(JobExecutionContext context) {
        LOG.trace("Executing TemporaryPayloadCleanerJob");
        temporaryPayloadService.cleanTemporaryPayloads();
    }
}
