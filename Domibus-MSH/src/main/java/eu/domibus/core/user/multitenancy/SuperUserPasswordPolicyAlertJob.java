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

import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Job in charge of sending alerts to super-users whose passwords expired or are about to expire
 */
@DisallowConcurrentExecution
public class SuperUserPasswordPolicyAlertJob extends GeneralQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SuperUserPasswordPolicyAlertJob.class);

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userService;

    @Override
    protected void executeJob(JobExecutionContext context) {

        LOG.debug("Executing job 'check password expiration' for super-users at " + LocalDateTime.now());

        userService.triggerPasswordAlerts();
    }

}
