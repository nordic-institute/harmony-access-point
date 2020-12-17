package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.util.DatabaseUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SplitAndJoinExpirationWorkerTest {

    @Tested
    SplitAndJoinExpirationWorker splitAndJoinExpirationWorker;

    @Injectable
    protected SplitAndJoinService splitAndJoinService;

    @Injectable
    private ConfigurationDAO configurationDAO;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Test
    public void executeJob_callPrivate(@Injectable JobExecutionContext context, @Injectable Domain domain) {
        new Expectations() {{
            authUtils.runWithSecurityContext((AuthenticatedProcedure)any, anyString, anyString);
        }};

        splitAndJoinExpirationWorker.executeJob(context, domain);

        new Verifications() {{
            AuthenticatedProcedure function;
            String username;
            String password;
            AuthRole role;
            authUtils.runWithSecurityContext(function = withCapture(),
                    username=withCapture(), password=withCapture());
            Assert.assertNotNull(function);
            Assert.assertEquals("splitAndJoinExpiration_user",username);
            Assert.assertEquals("splitAndJoinExpiration_password",password);

        }};
    }

    public void executeJob() {
        new Expectations() {{
            configurationDAO.configurationExists();
            result = true;
        }};

        splitAndJoinExpirationWorker.executeJob();

        new Verifications() {{
            splitAndJoinService.handleExpiredGroups();
        }};
    }
}