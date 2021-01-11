package eu.domibus.core.error;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @since 5.0
 * @author Catalin Enache
 */
@RunWith(JMockit.class)
public class ErrorLogCleanerJobTest {

    @Tested
    ErrorLogCleanerJob errorLogCleanerJob;

    @Injectable
    private ErrorService errorService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private AuthUtils authUtils;


    @Test
    public void executeJob(@Injectable JobExecutionContext context, @Injectable Domain domain) throws  Exception {

        errorLogCleanerJob.executeJob(context, domain);

        new FullVerifications() {{
            errorService.deleteErrorLogWithoutMessageIds();
        }};
    }
}