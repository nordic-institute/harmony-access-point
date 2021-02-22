package eu.domibus.core.message.retention;

import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author idragusa
 * @since 4.2.1
 */
@Service
public class UserMessageDeletionJobService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDeletionJobService.class);

    protected UserMessageDeletionJobDao userMessageDeletionJobDao;

    protected UserMessageLogDao userMessageLogDao;

    public UserMessageDeletionJobService(UserMessageDeletionJobDao userMessageDeletionJobDao, UserMessageLogDao userMessageLogDao) {
        this.userMessageDeletionJobDao = userMessageDeletionJobDao;
        this.userMessageLogDao = userMessageLogDao;
    }

    public void executeJob(UserMessageDeletionJob deletionJob) {
        setJobAsRunning(deletionJob);
        userMessageLogDao.deleteExpiredMessages(deletionJob.getStartRetentionDate(), deletionJob.getEndRetentionDate(), deletionJob.getMpc(), deletionJob.getMaxCount(), deletionJob.getProcedureName());
        setJobAsStopped(deletionJob);
    }

    public void setJobAsStopped(UserMessageDeletionJob deletionJob) {
        deletionJob.setState(UserMessageDeletionJobState.STOPPED.name());
        userMessageDeletionJobDao.update(deletionJob);
        LOG.debug("Stopped deletion job [{}]", deletionJob);
    }

    public void setJobAsRunning(UserMessageDeletionJob deletionJob) {
        deletionJob.setActualStartDate(new Date(System.currentTimeMillis()));
        deletionJob.setState(UserMessageDeletionJobState.RUNNING.name());
        userMessageDeletionJobDao.update(deletionJob);
        LOG.debug("Started deletion job [{}]", deletionJob);
    }

    public List<UserMessageDeletionJob> findCurrentDeletionJobs() {
        return userMessageDeletionJobDao.findCurrentDeletionJobs();
    }

    public void deleteJob(UserMessageDeletionJob deletionJob) {
        LOG.debug("Deletion job removed from database [{}]", deletionJob);
        userMessageDeletionJobDao.delete(deletionJob);
    }

    public void createJob(UserMessageDeletionJob deletionJob) {
        LOG.debug("Deletion job created in the database [{}]", deletionJob);
        userMessageDeletionJobDao.create(deletionJob);
    }

    public boolean doJobsOverlap(UserMessageDeletionJob currentDeletionJob, UserMessageDeletionJob newDeletionJob) {
        if (!currentDeletionJob.equals(newDeletionJob)) {
            return false;
        }
        if (newDeletionJob.getStartRetentionDate().before(currentDeletionJob.getStartRetentionDate()) &&
                newDeletionJob.getEndRetentionDate().before(currentDeletionJob.getStartRetentionDate())) {
            return false;
        }
        if (newDeletionJob.getStartRetentionDate().after(currentDeletionJob.getEndRetentionDate())) {
            return false;
        }
        return true;
    }


    public boolean isJobOverlaping(UserMessageDeletionJob deletionJob, List<UserMessageDeletionJob> currentDeletionJobs) {
        LOG.debug("Verify if deletion job overlaps current deletion jobs.");
        if (CollectionUtils.isEmpty(currentDeletionJobs)) {
            LOG.debug("No overlapping, there are no current deletion jobs.");
            return false;
        }

        List<UserMessageDeletionJob> result = currentDeletionJobs.stream().filter(currentDeletionJob -> doJobsOverlap(currentDeletionJob, deletionJob))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            LOG.debug("Deletion job does not overlap with current deletion jobs.");
            return false;
        }

        LOG.debug("Deletion job overlaps the following jobs [{}]", result);
        return true;
    }
}
