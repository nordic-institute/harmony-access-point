package eu.domibus.core.user.ui.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Job in charge of sending alerts to users whose passwords expired or are about to expire
 */
@DisallowConcurrentExecution
public class UserPasswordPolicyAlertJob extends DomibusQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserPasswordPolicyAlertJob.class);

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {

        LOG.debug("Executing job 'check password expiration' for users at " + LocalDateTime.now());

        userService.triggerPasswordAlerts();
    }

}
