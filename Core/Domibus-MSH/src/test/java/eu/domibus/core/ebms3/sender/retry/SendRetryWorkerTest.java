package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.pmode.ConfigurationDAO;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

import java.util.Arrays;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class SendRetryWorkerTest {

    public static final long MESSAGE_ID_1 = 123;
    public static final long MESSAGE_ID_2 = 456;
    public static final long MESSAGE_ID_3 = 789;

    private static List<Long> QUEUED_MESSAGEIDS = Arrays.asList(MESSAGE_ID_1, MESSAGE_ID_2, MESSAGE_ID_3);

    @Tested
    SendRetryWorker sendRetryWorker;

    @Injectable
    RetryService retryService;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    DomainService domainService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DatabaseUtil databaseUtil;

    @Injectable
    ConfigurationDAO configurationDAO;


    @Test
    public void executeJob(@Injectable JobExecutionContext context, @Injectable Domain domain) throws Exception {

        new Expectations(sendRetryWorker) {{
            retryService.getMessagesNotAlreadyScheduled();
            result = QUEUED_MESSAGEIDS;

            retryService.enqueueMessage(anyLong);

            configurationDAO.configurationExists();
            result = true;
        }};

        sendRetryWorker.executeJob(context, domain);

        new FullVerifications() {{
            retryService.enqueueMessage(MESSAGE_ID_1);
            retryService.enqueueMessage(MESSAGE_ID_2);
            retryService.enqueueMessage(MESSAGE_ID_3);
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {

        sendRetryWorker.setQuartzJobSecurityContext();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }};
    }
}