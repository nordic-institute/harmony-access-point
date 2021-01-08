package eu.domibus.core.user.ui.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.user.UserService;
import eu.domibus.core.util.DatabaseUtil;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class ActivateSuspendedUsersJobTest {

    @Tested
    ActivateSuspendedUsersJob activateSuspendedUsersJob;

    @Injectable
    private UserService userManagementService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    protected AuthUtils authUtils;

    @Test
    public void executeJob(@Mocked JobExecutionContext context, @Mocked Domain domain) {

        activateSuspendedUsersJob.executeJob(context, domain);

        new FullVerifications() {{
            userManagementService.reactivateSuspendedUsers();
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {
        activateSuspendedUsersJob.setQuartzJobSecurityContext();
        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext( "domibus-quartz",  "domibus-quartz", AuthRole.ROLE_AP_ADMIN);
        }};
    }
}