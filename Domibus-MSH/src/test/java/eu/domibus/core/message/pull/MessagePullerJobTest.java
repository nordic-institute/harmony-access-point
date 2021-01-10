package eu.domibus.core.message.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.message.MessageExchangeService;
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
public class MessagePullerJobTest {

    @Tested
    MessagePullerJob messagePullerJob;

    @Injectable
    private MessageExchangeService messageExchangeService;

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

        messagePullerJob.executeJob(context, domain);

        new FullVerifications() {{
            messageExchangeService.initiatePullRequest();
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {

        messagePullerJob.setQuartzJobSecurityContext();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password", AuthRole.ROLE_AP_ADMIN);
        }};
    }
}