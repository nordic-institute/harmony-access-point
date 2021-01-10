package eu.domibus.core.message.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.ebms3.sender.retry.RetryService;
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
 * @author Soumya Chandran
 * @since 5.0
 */
@RunWith(JMockit.class)
public class PullRetryWorkerTest {

    @Tested
    PullRetryWorker pullRetryWorker;

    @Injectable
    private RetryService retryService;

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

        pullRetryWorker.executeJob(context, domain);

        new FullVerifications() {{
            retryService.bulkExpirePullMessages();
            retryService.resetWaitingForReceiptPullMessages();
            retryService.bulkDeletePullMessages();
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {

        pullRetryWorker.setQuartzJobSecurityContext();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }};
    }
}