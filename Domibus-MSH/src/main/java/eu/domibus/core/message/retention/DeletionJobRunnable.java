package eu.domibus.core.message.retention;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * Class used to delete UserMessages in a new thread
 *
 * @author idragusa
 * @since 4.2.1
 */

public class DeletionJobRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DeletionJobRunnable.class);

    protected DeletionJob deletionJob;
    protected DeletionJobService deletionJobService;

    public DeletionJobRunnable(DeletionJobService deletionJobService, DeletionJob deletionJob) {
        this.deletionJobService = deletionJobService;
        this.deletionJob = deletionJob;
    }

    @Override
    public void run() {
        LOG.debug("Execute deletion job [{}]", deletionJob);
        deletionJobService.executeJob(deletionJob);
        LOG.debug("Deletion job executed [{}]", deletionJob);
    }
}
