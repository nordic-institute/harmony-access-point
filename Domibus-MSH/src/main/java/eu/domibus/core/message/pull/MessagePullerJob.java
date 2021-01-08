package eu.domibus.core.message.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
            messageExchangeService.initiatePullRequest();
        } catch (PModeException e) {
            LOG.warn("Invalid pmode configuration for pull request " + e.getMessage(), e);
        }
    }

    @Override
    protected void setQuartzJobSecurityContext() {
        authUtils.setAuthenticationToSecurityContext(DOMIBUS_QUARTZ_USER, DOMIBUS_QUARTZ_PASSWORD, AuthRole.ROLE_AP_ADMIN);
    }
}
