package eu.domibus.core.message.retention;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    private DomibusPropertyProvider domibusPropertyProvider;

    private PModeProvider pModeProvider;

    private UserMessageDeletionJobService userMessageDeletionJobService;

    private DomainTaskExecutor domainTaskExecutor;

    final static int DELETION_JOBS_DELTA = 60; // 1 hour in minutes

    public MessageRetentionStoredProcedureService(DomibusPropertyProvider domibusPropertyProvider,
                                                  PModeProvider pModeProvider,
                                                  UserMessageDeletionJobService userMessageDeletionJobService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.pModeProvider = pModeProvider;
        this.userMessageDeletionJobService = userMessageDeletionJobService;
    }

    @Override
    public boolean handlesDeletionStrategy(String retentionStrategy) {
        if (DeletionStrategy.STORED_PROCEDURE == DeletionStrategy.valueOf(retentionStrategy)) {
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

        LOG.debug("Using MessageRetentionStoredProcedureService to deleteExpiredMessages");

        final List<String> mpcs = pModeProvider.getMpcURIList();

        List<UserMessageDeletionJob> newDeletionJobs = getDeletionJobs(mpcs);
        List<UserMessageDeletionJob> currentDeletionJobs = cancelAndCleanExpiredJobs(userMessageDeletionJobService.findCurrentDeletionJobs());
        if (CollectionUtils.isNotEmpty(currentDeletionJobs)) {
            newDeletionJobs = filterOutOverlappingJobs(currentDeletionJobs, newDeletionJobs);
        }
        runDeletionJobs(newDeletionJobs);
    }

    protected List<UserMessageDeletionJob> cancelAndCleanExpiredJobs(List<UserMessageDeletionJob> deletionJobs) {
        if (CollectionUtils.isEmpty(deletionJobs)) {
            return null;
        }
        LOG.debug("Cancel deletion jobs if expired.");
        deletionJobs.stream().forEach(deletionJob -> cancelIfExpired(deletionJob));
        LOG.debug("Remove from database the deletion jobs in state [{}]", UserMessageDeletionJobState.STOPPED);
        deletionJobs.stream().filter(deletionJob -> !deletionJob.isActive()).forEach(deletionJob -> userMessageDeletionJobService.deleteJob(deletionJob));
        LOG.debug("Remove deletion jobs from current list of deletion jobs");
        List<UserMessageDeletionJob> runningDeletionJobs = deletionJobs.stream().filter(deletionJob -> deletionJob.isActive()).collect(Collectors.toList());
        LOG.debug("There are [{}] deletion jobs in state [{}]", UserMessageDeletionJobState.RUNNING);
        return runningDeletionJobs;
    }

    protected boolean cancelIfExpired(UserMessageDeletionJob deletionJob) {
        int timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_TIMEOUT);
        long expireTime = deletionJob.getActualStartDate().getTime() + timeout * 1000;
        if (System.currentTimeMillis() > expireTime) {
            LOG.debug("Cancel job [{}], expireTime [{}], currentTime [{}]", deletionJob, expireTime, System.currentTimeMillis());
            deletionJob.setState(UserMessageDeletionJobState.STOPPED.name());
            return true;
        }
        return false;
    }

    protected List<UserMessageDeletionJob> filterOutOverlappingJobs(List<UserMessageDeletionJob> currentDeletionJobs, List<UserMessageDeletionJob> newDeletionJobs) {

        List<UserMessageDeletionJob> deletionJobs = newDeletionJobs.stream()
                .filter(deletionJob -> !isJobOverlaping(deletionJob, currentDeletionJobs)).collect(Collectors.toList());

        return deletionJobs;
    }

    protected boolean isJobOverlaping(UserMessageDeletionJob deletionJob, List<UserMessageDeletionJob> currentDeletionJobs) {
        LOG.debug("Verify if new job overlaps currently running jobs.");
        if (CollectionUtils.isEmpty(currentDeletionJobs)) {
            LOG.debug("No overlapping, there are no current deletion jobs.");
            return false;
        }

        List<UserMessageDeletionJob> result = currentDeletionJobs.stream().filter(currentDeletionJob -> userMessageDeletionJobService.doJobsOverlap(currentDeletionJob, deletionJob))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            LOG.debug("Deletion job does not overlap with current deletion jobs.");
            return false;
        }

        LOG.debug("Deletion job overlaps the following jobs currently running [{}]", result);
        return true;
    }

    protected void runDeletionJobs(List<UserMessageDeletionJob> deletionJobs) {
        if (CollectionUtils.isEmpty(deletionJobs)) {
            LOG.debug("There is no deletion job to run.");
            return;
        }

        LOG.debug("Run deletion jobs if not already running on same interval.");
        deletionJobs.stream().forEach(deletionJob -> createAndRunDeletionJob(deletionJob));
    }

    protected void createAndRunDeletionJob(UserMessageDeletionJob deletionJob) {
        LOG.debug("Create and run deletion job [{}]", deletionJob);
        userMessageDeletionJobService.createJob(deletionJob);
        UserMessageDeletionJobRunnable userMessageDeletionJobRunnable = new UserMessageDeletionJobRunnable(userMessageDeletionJobService, deletionJob);
        domainTaskExecutor.submit(userMessageDeletionJobRunnable, false);
        LOG.debug("Deletion job submitted [{}]", deletionJob);
    }

    protected List<UserMessageDeletionJob> getDeletionJobs(List<String> mpcs) {
        LOG.debug("Build the list of deletion jobs based on the retention values for each mpc configured in the pMode.");
        List<UserMessageDeletionJob> deletionJobs = new ArrayList<>();
        for (final String mpc : mpcs) {
            LOG.debug("Create deletion jobs for mpc [{}]", mpc);
            checkMessageMetadata(mpc);
            addDeletionJobsToList(deletionJobs, mpc, MessageStatus.DOWNLOADED);
            addDeletionJobsToList(deletionJobs, mpc, MessageStatus.RECEIVED);
            addDeletionJobsToList(deletionJobs, mpc, MessageStatus.ACKNOWLEDGED);
        }
        LOG.debug("Created [{}] deletion jobs", deletionJobs.size());
        return deletionJobs;
    }

    protected List<UserMessageDeletionJob> addDeletionJobsToList(List<UserMessageDeletionJob> deletionJobs, final String mpc, final MessageStatus messageStatus) {
        final int parallelDeletionJobsNo = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_STORED_PROCEDURE_PARALLELDELETIONJOBSNO);
        final int deletionJobInterval = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_STORED_PROCEDURE_DELETIONJOBINTERVAL);
        int retention = getRetention(mpc, messageStatus);
        final int maxCount = getMaxCount(messageStatus);
        final String procedureName = getProcedureName(messageStatus);

        if (retention > 0) {
            LOG.debug("Retention value is > 0, creating deletion jobs for mpc [{}] and messageStatus [{}]", mpc, messageStatus);
            for (int i = 0; i < parallelDeletionJobsNo; i++) {
                retention = retention + deletionJobInterval * i;
                Date endDate = DateUtils.addMinutes(new Date(), (retention + DELETION_JOBS_DELTA) * -1);
                Date startDate = DateUtils.addMinutes(new Date(), (retention + deletionJobInterval) * -1);
                if (i == parallelDeletionJobsNo - 1) { // last job
                    startDate = new Date(deletionJobInterval * 1000);
                }
                UserMessageDeletionJob deletionJob = new UserMessageDeletionJob(mpc, startDate, endDate, maxCount, procedureName);
                LOG.debug("Created deletion job [{}]", deletionJob);
                deletionJobs.add(deletionJob);
            }
            return deletionJobs;
        }
        LOG.debug("Retention value is < 0, no deletion job created for mpc [{}] and messageStatus [{}]", mpc, messageStatus);
        return deletionJobs;
    }

    protected int getMaxCount(MessageStatus messageStatus) {
        int maxCount = -1;
        switch (messageStatus) {
            case DOWNLOADED:
                maxCount = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
                ;
                break;
            case RECEIVED:
                maxCount = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
                ;
                break;
            case ACKNOWLEDGED:
            case SEND_FAILURE:
                maxCount = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
                ;
                break;
        }
        LOG.debug("Get maxCount [{}] for messageStatus [{}]", maxCount, messageStatus);
        return maxCount;
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
                name = "DeleteExpiredSentMessages";
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
                retention = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
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
        if (StringUtils.isNotEmpty(fileLocation)) {
            LOG.warn("Payload is configured to be stored on the filesystem [{}]. This deletion service uses stored procedures which assume the payload is in the database.", fileLocation);
        }
    }

    protected void checkMessageMetadata(String mpc) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        if (!isDeleteMessageMetadata) {
            LOG.warn("DeleteMessageMetadata will be ignored for mpc [{}]. This retention service considers deleteMessageMetadata always true", mpc);
            return;
        }
    }
}
