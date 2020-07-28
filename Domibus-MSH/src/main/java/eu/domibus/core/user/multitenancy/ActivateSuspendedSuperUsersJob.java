package eu.domibus.core.user.multitenancy;

import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.scheduler.GeneralQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;

/**
 * @author Tiago Miguel
 * @since 4.0
 * <p>
 * Job in charge of unlocking suspended super user accounts.
 */
@DisallowConcurrentExecution
public class ActivateSuspendedSuperUsersJob extends GeneralQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ActivateSuspendedSuperUsersJob.class);

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userService;

    @Override
    protected void executeJob(JobExecutionContext context) {
        LOG.debug("Executing job to unlock suspended SUPER USER accounts at {}", new Date());
        userService.reactivateSuspendedUsers();
    }
}
