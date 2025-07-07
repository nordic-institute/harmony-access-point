package eu.domibus.core.diagnostics;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
@RunWith(JMockit.class)
public class DiagnosticsJobTest {

    @Tested
    DiagnosticsJob diagnosticsJob;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DiagnosticsService diagnosticsService;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DatabaseUtil databaseUtil;

    @Test
    public void executeJob(@Mocked JobExecutionContext context, @Mocked Domain domain) throws JobExecutionException {
        // Execute the job
        diagnosticsJob.executeJob(context, domain);

        // Verify that the diagnosticsService.logDiagnosticInfo() method was called
        new FullVerifications() {{
            diagnosticsService.logDiagnosticInfo();
        }};
    }
}
