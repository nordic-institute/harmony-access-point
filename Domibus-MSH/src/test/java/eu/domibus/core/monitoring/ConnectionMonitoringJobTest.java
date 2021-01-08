package eu.domibus.core.monitoring;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.util.DatabaseUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@RunWith(JMockit.class)
public class ConnectionMonitoringJobTest {
    @Tested
    ConnectionMonitoringJob connectionMonitoringJob;

    @Injectable
    private ConnectionMonitoringService connectionMonitoringService;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Test
    public void executeJob(@Mocked JobExecutionContext context, @Mocked Domain domain) throws JobExecutionException {

        new Expectations() {{
            connectionMonitoringService.isMonitoringEnabled();
            result = true;
        }};

        connectionMonitoringJob.executeJob(context, domain);

        new FullVerifications() {{
            connectionMonitoringService.sendTestMessages();
        }};
    }
}