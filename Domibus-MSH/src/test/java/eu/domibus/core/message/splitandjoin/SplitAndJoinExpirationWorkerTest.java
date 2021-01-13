package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.pmode.ConfigurationDAO;
import mockit.*;
import mockit.integration.junit4.JMockit;
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
    public void executeJob(@Injectable JobExecutionContext context, @Injectable Domain domain) {
        new Expectations() {{
            configurationDAO.configurationExists();
            result = true;
        }};

        splitAndJoinExpirationWorker.executeJob(context, domain);

        new Verifications() {{
            splitAndJoinService.handleExpiredGroups();
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {

        splitAndJoinExpirationWorker.setQuartzJobSecurityContext();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("splitAndJoinExpiration_user", "splitAndJoinExpiration_password");
        }};
    }
}