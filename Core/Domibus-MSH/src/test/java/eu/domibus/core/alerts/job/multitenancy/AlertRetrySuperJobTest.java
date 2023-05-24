package eu.domibus.core.alerts.job.multitenancy;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.service.AlertService;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class AlertRetrySuperJobTest {

    @Tested
    AlertRetrySuperJob alertRetrySuperJob;

    @Injectable
    private AlertService alertService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Test
    public void executeJob(@Injectable JobExecutionContext context) {

        alertRetrySuperJob.executeJob(context);

        new FullVerifications() {{
            alertService.retrieveAndResendFailedAlerts();
        }};

    }
}