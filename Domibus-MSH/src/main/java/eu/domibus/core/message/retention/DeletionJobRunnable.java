package eu.domibus.core.message.retention;

import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.Date;

/**
 * Class used to delete UserMessages in a new thread
 *
 * @author idragusa
 * @since 4.2.1
 */

public class DeletionJobRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DeletionJobRunnable.class);

    protected UserMessageLogDao userMessageLogDao;
    protected DeletionJobDao deletionJobDao;
    protected DeletionJob deletionJob;

    public DeletionJobRunnable(UserMessageLogDao userMessageLogDao, DeletionJobDao deletionJobDao, DeletionJob deletionJob) {
        this.userMessageLogDao = userMessageLogDao;
        this.deletionJobDao = deletionJobDao;
        this.deletionJob = deletionJob;
    }

    @Override
    public void run() {
        Date currentDate = new Date(System.currentTimeMillis());
        LOG.debug("Deleting expired messages for deletion job [{}]", deletionJob);
        deletionJob.setActualStartDate(currentDate);
        deletionJob.setState(DeletionJobState.RUNNING.name());
        deletionJobDao.update(deletionJob);
        //userMessageLogDao.deleteExpiredMessages(startDate, mpc, limit, procedureName);
        sleep(30);
        LOG.debug("Deletion job executed [{}]", deletionJob);
        deletionJob.setState(DeletionJobState.STOPPED.name());
        deletionJobDao.update(deletionJob);
        LOG.debug("Marked job as done [{}], ", deletionJob);
    }

    protected void sleep(long seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            LOG.warn("Sleep Interrupted");
        }
    }
}
