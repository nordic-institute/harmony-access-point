package eu.domibus.plugin.webService.backend.reliability.retry;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginMessageSender;
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
    protected WSPluginBackendRetryService retryService;

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
            retryService.sendWaitingForRetry();
            times = 1;
        }};
    }
}