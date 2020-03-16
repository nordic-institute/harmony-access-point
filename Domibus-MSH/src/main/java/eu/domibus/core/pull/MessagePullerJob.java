package eu.domibus.core.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class MessagePullerJob extends DomibusQuartzJobBean {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagePullerJob.class);

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        try {
            if (!authUtils.isUnsecureLoginAllowed()) {
                authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password", AuthRole.ROLE_AP_ADMIN);
            }
            messageExchangeService.initiatePullRequest();
        } catch (PModeException e) {
            LOG.warn("Invalid pmode configuration for pull request " + e.getMessage(), e);
        }
    }
}
