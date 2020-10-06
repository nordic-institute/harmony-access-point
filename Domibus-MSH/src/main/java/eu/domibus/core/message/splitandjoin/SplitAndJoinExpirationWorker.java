package eu.domibus.core.message.splitandjoin;

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
 * @author Cosmin Baciu
 * @since 4.1
 */
@DisallowConcurrentExecution
public class SplitAndJoinExpirationWorker extends DomibusQuartzJobBean {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinExpirationWorker.class);

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    private ConfigurationDAO configurationDAO;

    @Autowired
    private AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        LOG.debug("SplitAndJoinExpirationWorker executed");

        authUtils.wrapApplicationSecurityContextToMethod(this::executeJob,
                "splitAndJoinExpiration_user", "splitAndJoinExpiration_password");
    }

    protected void executeJob() {
        if (!configurationDAO.configurationExists()) {
            LOG.debug("Could not checked for expired SplitAndJoin messages: PMode is not configured");
            return;
        }
        splitAndJoinService.handleExpiredGroups();
    }
}
