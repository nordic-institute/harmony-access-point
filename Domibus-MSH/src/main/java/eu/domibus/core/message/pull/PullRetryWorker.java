package eu.domibus.core.message.pull;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.ebms3.sender.retry.RetryService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class PullRetryWorker extends DomibusQuartzJobBean {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PullRetryWorker.class);

    @Autowired
    protected RetryService retryService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {

        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }

        try {
            retryService.bulkExpirePullMessages();
        } catch (Exception e) {
            LOG.error("Error while bulk expiring pull messages.", e);
        }

        try {
            retryService.resetWaitingForReceiptPullMessages();
        } catch (Exception e) {
            LOG.error("Error while resetting waiting for receipt.", e);
        }

        try {
            retryService.bulkDeletePullMessages();
        } catch (Exception e) {
            LOG.error("Error while bulk deleting messages.", e);
        }
    }
}
