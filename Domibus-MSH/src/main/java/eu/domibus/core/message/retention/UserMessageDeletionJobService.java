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
public class UserMessageDeletionJobService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDeletionJobService.class);

    @Autowired
    protected UserMessageDeletionJobDao userMessageDeletionJobDao;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    protected void executeJob(UserMessageDeletionJob deletionJob) {
        setJobAsRunning(deletionJob);
        userMessageLogDao.deleteExpiredMessages(deletionJob.getStartRetentionDate(), deletionJob.getEndRetentionDate(), deletionJob.getMpc(), deletionJob.getMaxCount(), deletionJob.getProcedureName());
        setJobAsStopped(deletionJob);
    }

    protected void setJobAsStopped(UserMessageDeletionJob deletionJob) {
        deletionJob.setState(UserMessageDeletionJobState.STOPPED.name());
        userMessageDeletionJobDao.update(deletionJob);
        LOG.debug("Stopped deletion job [{}]", deletionJob);
    }

    protected void setJobAsRunning(UserMessageDeletionJob deletionJob) {
        deletionJob.setActualStartDate(new Date(System.currentTimeMillis()));
        deletionJob.setState(UserMessageDeletionJobState.RUNNING.name());
        userMessageDeletionJobDao.update(deletionJob);
        LOG.debug("Started deletion job [{}]", deletionJob);
    }

    protected List<UserMessageDeletionJob> findCurrentDeletionJobs() {
        return userMessageDeletionJobDao.findCurrentDeletionJobs();
    }

    protected void deleteJob(UserMessageDeletionJob deletionJob) {
        LOG.debug("Deletion job removed from database [{}]", deletionJob);
        userMessageDeletionJobDao.delete(deletionJob);
    }

    protected void createJob(UserMessageDeletionJob deletionJob) {
        LOG.debug("Deletion job created in the database [{}]", deletionJob);
        userMessageDeletionJobDao.create(deletionJob);
    }

    protected boolean doJobsOverlap(UserMessageDeletionJob currentDeletionJob, UserMessageDeletionJob newDeletionJob) {
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
