package eu.domibus.core.message.resend;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Job used to restore all failed messages
 * @author Soumya Chandran
 * @since 5.1
 */
@DisallowConcurrentExecution
public class MessageResendJob extends DomibusQuartzJobBean {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageResendJob.class);

    @Autowired
    private UserMessageRestoreService restoreService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        LOG.debug("Executing job to resend messages in failed status..");
        restoreService.findAndRestoreFailedMessages();
    }

}
