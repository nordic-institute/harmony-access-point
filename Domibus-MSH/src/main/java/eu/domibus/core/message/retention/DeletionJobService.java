package eu.domibus.core.message.retention;

import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 *
 * @author idragusa
 * @since 4.2.1
 */
@Service
public class DeletionJobService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DeletionJobService.class);

    @Autowired
    protected DeletionJobDao deletionJobDao;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    protected void executeJob(DeletionJob deletionJob) {
        setJobAsRunning(deletionJob);
        //userMessageLogDao.deleteExpiredMessages(deletionJob.getStartRetentionDate(), deletionJob.getEndRetentionDate(), deletionJob.getMpc(), deletionJob.getMaxCount(), deletionJob.getProcedureName());
        sleep(30);
        setJobAsStopped(deletionJob);
    }


    protected void sleep(long seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            LOG.warn("Sleep Interrupted");
        }
    }

    protected void setJobAsStopped(DeletionJob deletionJob) {
        deletionJob.setState(DeletionJobState.STOPPED.name());
        deletionJobDao.update(deletionJob);
        LOG.debug("Stopped deletion job [{}]", deletionJob);
    }

    protected void setJobAsRunning(DeletionJob deletionJob) {
        deletionJob.setActualStartDate(new Date(System.currentTimeMillis()));
        deletionJob.setState(DeletionJobState.RUNNING.name());
        deletionJobDao.update(deletionJob);
        LOG.debug("Started deletion job [{}]", deletionJob);
    }

    protected List<DeletionJob> findCurrentDeletionJobs() {
        return deletionJobDao.findCurrentDeletionJobs();
    }

    protected void deleteJob(DeletionJob deletionJob) {
        LOG.debug("Deletion job removed from database [{}]", deletionJob);
        deletionJobDao.delete(deletionJob);
    }

    protected void createJob(DeletionJob deletionJob) {
        LOG.debug("Deletion job created in the database [{}]", deletionJob);
        deletionJobDao.create(deletionJob);
    }

    protected boolean doJobsOverlap(DeletionJob currentDeletionJob, DeletionJob newDeletionJob) {
        if(!currentDeletionJob.equals(newDeletionJob)) {
            return false;
        }
        if(newDeletionJob.getStartRetentionDate().before(currentDeletionJob.getStartRetentionDate()) &&
                newDeletionJob.getEndRetentionDate().before(currentDeletionJob.getStartRetentionDate())) {
            return false;
        }
        if(newDeletionJob.getStartRetentionDate().after(currentDeletionJob.getEndRetentionDate())) {
            return false;
        }
        return true;
    }
}
