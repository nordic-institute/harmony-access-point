package eu.domibus.core.ebms3.sender.retry;


import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.ebms3.sender.MessageSenderService;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Quartz based worker responsible for the periodical execution of {@link MessageSenderService#sendUserMessage(String, int)}
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class SendRetryWorker extends DomibusQuartzJobBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SendRetryWorker.class);

    @Autowired
    protected RetryService retryService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    protected void executeJob(final JobExecutionContext context, final Domain domain) throws JobExecutionException {
        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("retry_user", "retry_password");
        }

        try {
            final List<String> messagesNotAlreadyQueued = retryService.getMessagesNotAlreadyScheduled();

            for (final String messageId : messagesNotAlreadyQueued) {
                retryService.enqueueMessage(messageId);
            }
        } catch (Exception e) {
            LOG.error("Error while enqueueing messages.", e);
        }


    }
}
