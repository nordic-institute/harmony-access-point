package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Christian Koch, Stefan Mueller
 */
@DisallowConcurrentExecution
public class RetentionWorker extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionWorker.class);

    @Autowired
    protected MessageRetentionService messageRetentionService;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    private AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        if (configurationDAO.configurationExists()) {
            messageRetentionService.deleteExpiredMessages();
        }
    }

    @Override
    protected void setQuartzJobSecurityContext() {
        authUtils.setAuthenticationToSecurityContext("retention_user", "retention_password");
    }
}
