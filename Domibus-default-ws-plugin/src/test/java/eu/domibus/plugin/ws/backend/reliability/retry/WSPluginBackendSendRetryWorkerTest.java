package eu.domibus.plugin.ws.backend.reliability.retry;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.dispatch.WSPluginMessageSender;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class WSPluginBackendSendRetryWorkerTest {

    @Injectable
    protected WSPluginBackendScheduleRetryService retryService;

    @Injectable
    protected WSPluginMessageSender wsPluginMessageSender;

    @Injectable
    protected DomainExtService domainExtService;

    @Injectable
    protected DomainContextExtService domainContextExtService;

    @Tested
    protected WSPluginBackendSendRetryWorker retryWorker;

    @Test
    public void executeJob(@Mocked WSBackendMessageLogEntity entity1,
                           @Mocked WSBackendMessageLogEntity entity2) {

        retryWorker.executeJob(null, null);

        new FullVerifications() {{
            retryService.scheduleWaitingForRetry();
            times = 1;
        }};
    }
}