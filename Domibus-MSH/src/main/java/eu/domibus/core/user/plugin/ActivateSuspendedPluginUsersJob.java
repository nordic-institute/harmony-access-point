package eu.domibus.core.user.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Job in charge of unlocking suspended plugin user accounts.
 */
@DisallowConcurrentExecution
public class ActivateSuspendedPluginUsersJob extends DomibusQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ActivateSuspendedPluginUsersJob.class);

    @Autowired
    private PluginUserServiceImpl userService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {

        LOG.debug("Executing job to unlock suspended plugin accounts at " + new Date());

        userService.reactivateSuspendedUsers();
    }

}
