package eu.domibus.core.message.retention;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * This service class is responsible for the retention and clean up of Domibus messages.
 * This service uses the store procedures approach
 *
 * @author idragusa
 * @since 4.2.1
 */
@Service
public class MessageRetentionStoredProcedureService implements MessageRetentionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetentionStoredProcedureService.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected DeletionJobDao deletionJobDao;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    protected Integer expiredDownloadedMessagesLimit;
    protected Integer expiredNotDownloadedMessagesLimit;
    protected Integer expiredSentMessagesLimit;
    protected Integer timeout;

    @Override
    public boolean handlesDeletionStrategy(String retentionStrategy) {
        if(DeletionStrategy.STORED_PROCEDURE.compareTo(DeletionStrategy.valueOf(retentionStrategy))== 0) {
            checkFileLocation();
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Timer(clazz = MessageRetentionStoredProcedureService.class, value = "retention_deleteExpiredMessages")
    @Counter(clazz = MessageRetentionStoredProcedureService.class, value = "retention_deleteExpiredMessages")
    public void deleteExpiredMessages() {
        expiredDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
        expiredNotDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
        expiredSentMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
        timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_TIMEOUT);

        LOG.debug("Using MessageRetentionStoredProcedureService to deleteExpiredMessages");

        final List<String> mpcs = pModeProvider.getMpcURIList();

        List<DeletionJob> newDeletionJobs = getDeletionJobs(mpcs);
        List<DeletionJob> currentDeletionJobs = deletionJobDao.findCurrentDeletionJobs();

        if(CollectionUtils.isEmpty(currentDeletionJobs)) {
            LOG.debug("There are no current deletion jobs stored in the database.");
            storeDeletionJobs(newDeletionJobs);
            runDeletionJobs(newDeletionJobs);
            return;
        }

        if(sameJobs(currentDeletionJobs, newDeletionJobs)) {
            LOG.debug("There are similar deletion jobs stored in the database.");
            runDeletionJobs(currentDeletionJobs, newDeletionJobs);
            return;
        }

        LOG.debug("There are different deletion jobs stored in the database.");
        if(verifyAllJobsAreDone(currentDeletionJobs)) {
            LOG.debug("All current jobs stored in the database ended or expired");
            removeDeletionJobs(currentDeletionJobs);
            storeDeletionJobs(newDeletionJobs);
            runDeletionJobs(newDeletionJobs);
            return;
        }
    }

    protected void cancelExpiredJobs(List<DeletionJob> deletionJobs) {
        LOG.debug("Cancel [{}] deletion jobs if expired.", deletionJobs.size());
        deletionJobs.stream().forEach(deletionJob -> cancelIfExpired(deletionJob));
    }

    protected boolean cancelIfExpired(DeletionJob deletionJob) {
        long expireTime = deletionJob.getActualStartDate().getTime()+timeout*1000;
        if(System.currentTimeMillis() > expireTime) {
            LOG.debug("Cancel job [{}], expireTime [{}], currentTime [{}]", deletionJob, expireTime, System.currentTimeMillis());
            deletionJob.setState(DeletionJobState.STOPPED.name());
            return true;
        }
        return false;
    }

    protected void removeDeletionJobs(List<DeletionJob> deletionJobs) {
        LOG.debug("Remove [{}] deletion jobs", deletionJobs.size());
        if (CollectionUtils.isNotEmpty(deletionJobs)) {
            deletionJobs.stream().forEach(deletionJob -> deletionJobDao.delete(deletionJob));
        }
    }

    protected void storeDeletionJobs(List<DeletionJob> deletionJobs) {
        LOG.debug("Store [{}] new deletion jobs", deletionJobs.size());
        if(CollectionUtils.isNotEmpty(deletionJobs)) {
            deletionJobs.stream().forEach(deletionJob -> deletionJobDao.create(deletionJob));
        }
    }

    protected void runDeletionJobs(List<DeletionJob> currentDeletionJobs, List<DeletionJob> newDeletionJobs) {
        LOG.debug("Filter out the jobs that are already running.");
        List<DeletionJob> deletionJobs = newDeletionJobs.stream().filter(deletionJob -> !isDeletionJobRunning(currentDeletionJobs, deletionJob)).collect(Collectors.toList());
        runDeletionJobs(deletionJobs);
    }

    protected void runDeletionJobs(List<DeletionJob> newDeletionJobs) {
        LOG.debug("Run [{}] deletion jobs", newDeletionJobs.size());
        newDeletionJobs.stream().forEach(deletionJob ->runDeletionJob(deletionJob));
    }

    protected boolean isDeletionJobRunning(List<DeletionJob> currentDeletionJobs, DeletionJob deletionJob) {
        DeletionJob currentJob = currentDeletionJobs.stream().filter(currentDeletionJob -> deletionJob.equals(currentDeletionJob)).collect(Collectors.toList()).get(0);
        if(currentJob.isActive() && !cancelIfExpired(currentJob)) {
            LOG.debug("Deletion job is running [{}]", deletionJob);
            return true;
        }
        LOG.debug("Deletion job is not running [{}]", deletionJob);
        return false;
    }

    protected void runDeletionJob(DeletionJob deletionJob) {
        LOG.debug("Starting deletion job [{}]", deletionJob);
        DeletionJobRunnable deletionJobRunnable = new DeletionJobRunnable(userMessageLogDao, deletionJobDao, deletionJob);
        domainTaskExecutor.submit(deletionJobRunnable, false);
        LOG.debug("Deletion job ended [{}]", deletionJob);
    }

    protected boolean verifyAllJobsAreDone(List<DeletionJob> deletionJobs) {
        LOG.info("Verify if jobs are done [{}]", deletionJobs.size());
        Set<DeletionJob> activeJobs = deletionJobs.stream().filter(deletionJob->deletionJob.isActive()).collect(Collectors.toSet());
        if(CollectionUtils.isNotEmpty(activeJobs)) {
            cancelExpiredJobs(deletionJobs);
            return false;
        }
        return true;
    }

    protected boolean sameJobs(List<DeletionJob> currentJobs, List<DeletionJob> newJobs) {
        LOG.info("Verify if new jobs are the same with current jobs.");
        if(currentJobs.size() != newJobs.size()) {
            LOG.debug("The number of jobs is different.");
            return false;
        }

        Set<DeletionJob> result = currentJobs.stream()
                .filter(newJobs::contains)
                .collect(Collectors.toSet());
        if(result.isEmpty()) {
            LOG.info("All jobs are the same.");
            return true;
        }

        LOG.info("Jobs are different.");
        return false;
    }

    protected List<DeletionJob> getDeletionJobs(List<String> mpcs) {
        LOG.debug("Build the list of deletion jobs based on the retention values for each mpc configured in the pMode.");
        List<DeletionJob> deletionJobs = new ArrayList<>();
        for (final String mpc : mpcs) {
            LOG.debug("Create deletion jobs for mpc [{}]", mpc);
            checkMessageMetadata(mpc);
            deletionJobs.add(getDeletionJob(mpc, MessageStatus.DOWNLOADED));
            deletionJobs.add(getDeletionJob(mpc, MessageStatus.RECEIVED));
            deletionJobs.add(getDeletionJob(mpc, MessageStatus.ACKNOWLEDGED));
        }
        return deletionJobs;
    }

    protected DeletionJob getDeletionJob(String mpc, MessageStatus messageStatus) {
        final int retention = getRetention(mpc, messageStatus);
        final int limit = getLimit(messageStatus);
        final String procedureName = getProcedureName(messageStatus);

        if(retention > 0) {
            DeletionJob deletionJob = new DeletionJob(mpc, retention, limit, procedureName);
            LOG.debug("Created deletion job [{}]", deletionJob);
            return deletionJob;
        }
        LOG.debug("Retention value is < 0, no deletion job created for mpc [{}] and messageStatus [{}]", mpc, messageStatus);
        return null;
    }

    protected int getLimit(MessageStatus messageStatus) {
        int limit = -1;
        switch (messageStatus) {
            case DOWNLOADED:
                limit = expiredDownloadedMessagesLimit;
                break;
            case RECEIVED:
                limit = expiredNotDownloadedMessagesLimit;
                break;
            case ACKNOWLEDGED:
            case SEND_FAILURE:
                limit = expiredSentMessagesLimit;
                break;
        }
        LOG.debug("Get limit [{}] for messageStatus [{}]", limit, messageStatus);
        return limit;
    }

    protected String getProcedureName(MessageStatus messageStatus) {
        String name = null;
        switch (messageStatus) {
            case DOWNLOADED:
                name = "DeleteExpiredDownloadedMessages";
                break;
            case RECEIVED:
                name = "DeleteExpiredUnDownloadedMessages";
                break;
            case ACKNOWLEDGED:
            case SEND_FAILURE:
                name =  "DeleteExpiredSentMessages";
                break;
            default:
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "There is no stored procedure for this message status" + messageStatus.name());
        }
        LOG.debug("Get procedure name [{}] for messageStatus [{}]", name, messageStatus);
        return name;
    }

    protected int getRetention(String mpc, MessageStatus messageStatus) {
        int retention = -1;
        switch (messageStatus) {
            case DOWNLOADED:
                retention = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
                break;
            case RECEIVED:
                retention = pModeProvider.getRetentionUndownloadedByMpcName(mpc);
                break;
            case ACKNOWLEDGED:
            case SEND_FAILURE:
                retention = pModeProvider.getRetentionSentByMpcURI(mpc);
                break;
        }
        LOG.debug("Retention value for mpc [{}] and messageStatus [{}] is [{}]", mpc, messageStatus, retention);
        return retention;
    }

    protected void checkFileLocation() {
        String fileLocation = domibusPropertyProvider.getProperty(DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
        if(StringUtils.isNotEmpty(fileLocation)) {
            LOG.warn("Payload is configured to be stored on the filesystem [{}]. This deletion service uses stored procedures which assume the payload is in the database.", fileLocation);
        }
    }

    protected void checkMessageMetadata(String mpc) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        if( !isDeleteMessageMetadata ){
            LOG.warn("DeleteMessageMetadata will be ignored for mpc [{}]. This retention service considers deleteMessageMetadata always true", mpc);
            return;
        }
    }

    protected Integer getRetentionValue(String propertyName) {
        return domibusPropertyProvider.getIntegerProperty(propertyName);
    }
}
