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
 * @author Ion Perpegel
 * @since 5.1
 */
@DisallowConcurrentExecution
public class ConnectionMonitoringSelfJob extends DomibusQuartzJobBean {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringSelfJob.class);

    @Autowired
    protected ConnectionMonitoringService connectionMonitoringService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        if (!connectionMonitoringService.isSelfMonitoringEnabled()) {
            return;
        }

        LOG.debug("ConnectionMonitoringSelfJob started on [{}] domain", domain);
        connectionMonitoringService.sendTestMessageToMyself();
        LOG.debug("ConnectionMonitoringSelfJob ended on [{}] domain", domain);
    }
}
