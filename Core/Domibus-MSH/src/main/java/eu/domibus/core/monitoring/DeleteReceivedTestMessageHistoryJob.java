package eu.domibus.core.monitoring;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Job that's deleting old test messages on the receiving side (c3)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@DisallowConcurrentExecution
public class DeleteTestMessageHistoryJob extends DomibusQuartzJobBean {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DeleteTestMessageHistoryJob.class);

    @Autowired
    protected ConnectionMonitoringService connectionMonitoringService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        if (!connectionMonitoringService.isDeleteHistoryEnabled()) {
            return;
        }

        LOG.debug("DeleteTestMessageHistoryJob started on [{}] domain", domain);
        connectionMonitoringService.deleteReceivedTestMessageHistory();
        LOG.debug("DeleteTestMessageHistoryJob ended on [{}] domain", domain);
    }
}
