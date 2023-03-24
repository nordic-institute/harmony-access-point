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
 * Job that's deleting test message history on the receiving side (c3)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@DisallowConcurrentExecution
public class DeleteReceivedTestMessageHistoryJob extends DomibusQuartzJobBean {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DeleteReceivedTestMessageHistoryJob.class);

    @Autowired
    protected ConnectionMonitoringService connectionMonitoringService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        LOG.debug("DeleteReceivedTestMessageHistoryJob started on [{}] domain", domain);
        connectionMonitoringService.deleteReceivedTestMessageHistory();
        LOG.debug("DeleteTestMessageHistoryJob ended on [{}] domain", domain);
    }
}
