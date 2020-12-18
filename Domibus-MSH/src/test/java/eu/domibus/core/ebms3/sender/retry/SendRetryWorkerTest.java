package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import eu.domibus.core.util.DatabaseUtil;
import eu.domibus.api.util.DatabaseUtil;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
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

    public static final String MESSAGE_ID_1 = "queued123@domibus.eu";
    public static final String MESSAGE_ID_2 = "queued456@domibus.eu";
    public static final String MESSAGE_ID_3 = "queued789@domibus.eu";

    private static List<String> QUEUED_MESSAGEIDS = Arrays.asList(MESSAGE_ID_1, MESSAGE_ID_2, MESSAGE_ID_3);

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

    @Test
    public void test_executeJob_wrapSecurityContext(final @Injectable JobExecutionContext jobExecutionContext,
                                final @Injectable Domain domain) throws Exception {

        new Expectations() {{
            authUtils.runWithSecurityContext((AuthenticatedProcedure)any, anyString, anyString);
        }};

        sendRetryWorker.executeJob(jobExecutionContext, domain);

        new FullVerifications() {{
            AuthenticatedProcedure function;
            String username;
            String password;
            authUtils.runWithSecurityContext(function = withCapture(),
                    username=withCapture(), password=withCapture());
            Assert.assertNotNull(function);
            Assert.assertEquals("retry_user",username);
            Assert.assertEquals("retry_password",password);
        }};
    }

    @Test
    public void test_executeJob_private() throws Exception {

        new Expectations(sendRetryWorker) {{

            retryService.getMessagesNotAlreadyScheduled();
            result = QUEUED_MESSAGEIDS;

            retryService.enqueueMessage(anyString);
        }};

        sendRetryWorker.executeJob();

        new FullVerifications() {{
            retryService.enqueueMessage(MESSAGE_ID_1);
            retryService.enqueueMessage(MESSAGE_ID_2);
            retryService.enqueueMessage(MESSAGE_ID_3);
        }};
    }
}