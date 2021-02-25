package eu.domibus.core.message.retention;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * Class used to delete UserMessages in a new thread
 *
 * @author idragusa
 * @since 4.2.1
 */
public class UserMessageDeletionJobRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDeletionJobRunnable.class);

    protected UserMessageDeletionJobEntity deletionJob;
    protected UserMessageDeletionJobService userMessageDeletionJobService;

    public UserMessageDeletionJobRunnable(UserMessageDeletionJobService userMessageDeletionJobService, UserMessageDeletionJobEntity deletionJob) {
        this.userMessageDeletionJobService = userMessageDeletionJobService;
        this.deletionJob = deletionJob;
    }

    @Override
    public void run() {
        LOG.trace("Execute deletion job [{}]", deletionJob);
        userMessageDeletionJobService.executeJob(deletionJob);
        LOG.trace("Deletion job executed [{}]", deletionJob);
    }
}
