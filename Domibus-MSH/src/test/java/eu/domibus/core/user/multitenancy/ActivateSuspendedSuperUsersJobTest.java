package eu.domibus.core.user.multitenancy;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.user.UserService;
import eu.domibus.core.util.DatabaseUtil;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ActivateSuspendedSuperUsersJobTest {

    @Tested
    ActivateSuspendedSuperUsersJob activateSuspendedSuperUsersJob;


    @Injectable
    private UserService userManagementService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Injectable
    private DomainContextProvider domainContextProvider;


    @Test
    public void executeJob(@Injectable JobExecutionContext context) {
        activateSuspendedSuperUsersJob.executeJob(context);

        new FullVerifications() {{
            userManagementService.reactivateSuspendedUsers();
        }};
    }

}