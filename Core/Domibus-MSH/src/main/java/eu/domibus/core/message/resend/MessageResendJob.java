package eu.domibus.core.message.resend;

import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.core.message.UserMessageRestoreDao;
import eu.domibus.core.scheduler.DomibusQuartzJobBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private UserMessageRestoreDao userMessageRestoreDao;

    @Transactional
    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) throws JobExecutionException {
        final List<String> restoredMessages = new ArrayList<>();
        List<String> messageIds = restoreService.findAllMessagesToRestore();
        for (String messageId : messageIds) {
            LOG.debug("Found message to restore. Starting the restoring process of message with messageId [{}]", messageId);
            try {
                restoreService.restoreFailedMessage(messageId);
                restoredMessages.add(messageId);
                userMessageRestoreDao.delete(messageId);
            } catch (Exception e) {
                userMessageRestoreDao.delete(messageId);
                LOG.error("Failed to restore message [" + messageId + "]", e);
                throw new MessagingException("Failed to restore message: " + messageId, null);
            }
        }
        LOG.debug("Restoring process of failed messages completed successfully.");
    }

}
