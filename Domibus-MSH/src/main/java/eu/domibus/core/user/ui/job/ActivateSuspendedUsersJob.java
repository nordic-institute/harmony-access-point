package eu.domibus.core.user.ui.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Job in charge of unlocking suspended user accounts.
 */
@DisallowConcurrentExecution
public class ActivateSuspendedUsersJob extends DomibusQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ActivateSuspendedUsersJob.class);

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {

        LOG.debug("Executing job to unlock suspended accounts at {}", new Date());

        authUtils.runWithDomibusSecurityContext(() -> userService.reactivateSuspendedUsers(), AuthRole.ROLE_AP_ADMIN, true);
    }

}
