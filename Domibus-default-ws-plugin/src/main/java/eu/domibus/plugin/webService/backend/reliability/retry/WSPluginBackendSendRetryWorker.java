package eu.domibus.plugin.webService.backend.reliability.retry;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.quartz.DomibusQuartzJobExtBean;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz based worker responsible for the periodical execution of
 * {@link eu.domibus.plugin.webService.backend.dispatch.WSPluginMessageSender#sendNotification(WSBackendMessageLogEntity)}
 *
 * @author Francois Gautier
 * @since 5.0
 */

@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class WSPluginBackendSendRetryWorker extends DomibusQuartzJobExtBean {

    @Autowired
    protected WSPluginBackendScheduleRetryService retryService;

    @Override
    protected void executeJob(final JobExecutionContext context, DomainDTO domain) {
        retryService.scheduleWaitingForRetry();
    }
}
