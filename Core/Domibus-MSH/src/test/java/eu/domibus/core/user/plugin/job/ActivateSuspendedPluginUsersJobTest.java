package eu.domibus.core.user.plugin.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.plugin.PluginUserServiceImpl;
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
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class ActivateSuspendedPluginUsersJobTest {

    @Tested
    ActivateSuspendedPluginUsersJob activateSuspendedPluginUsersJob;

    @Injectable
    private PluginUserServiceImpl pluginUserService;

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
    public void executeJob(@Mocked JobExecutionContext context, @Mocked Domain domain) throws JobExecutionException {

        activateSuspendedPluginUsersJob.executeJob(context, domain);

        new FullVerifications() {{
            pluginUserService.reactivateSuspendedUsers();
        }};
    }

    @Test
    public void setQuartzJobSecurityContext() {

        activateSuspendedPluginUsersJob.setQuartzJobSecurityContext();

        new FullVerifications() {{
            authUtils.setAuthenticationToSecurityContext("domibus-quartz", "domibus-quartz", AuthRole.ROLE_AP_ADMIN);
        }};
    }
}