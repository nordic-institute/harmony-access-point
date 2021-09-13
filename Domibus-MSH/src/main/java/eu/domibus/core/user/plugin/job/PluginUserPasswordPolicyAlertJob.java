package eu.domibus.core.user.plugin.job;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.core.user.plugin.PluginUserService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * Job in charge of sending alerts about plugin users whose passwords expired or are about to expire
 */
@DisallowConcurrentExecution
public class PluginUserPasswordPolicyAlertJob extends DomibusQuartzJobBean {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserPasswordPolicyAlertJob.class);

    @Autowired
    private PluginUserService pluginUserService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {

        LOG.debug("Executing job 'check password expiration' for users at " + LocalDateTime.now(ZoneOffset.UTC));

        pluginUserService.triggerPasswordAlerts();
    }

}
