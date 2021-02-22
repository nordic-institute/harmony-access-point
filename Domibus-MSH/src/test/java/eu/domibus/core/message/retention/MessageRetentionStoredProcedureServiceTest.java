package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author idragusa
 * @since 4.2.1
 */
@RunWith(JMockit.class)
public class MessageRetentionStoredProcedureServiceTest {

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected UserMessageDeletionJobService userMessageDeletionJobService;

    @Injectable
    protected DomainTaskExecutor domainTaskExecutor;

    @Tested
    MessageRetentionStoredProcedureService messageRetentionService;

    final String mpc1 = "mpc1";
    final String mpc2 = "mpc2";
    final Integer expiredDownloadedMessagesLimit = 10;
    final Integer expiredNotDownloadedMessagesLimit = 20;
    final Integer expiredSentMessagesLimit = 30;
    final Integer timeout = 300; // 5 min
    final Integer parallelDeletionJobsNo = 2;
    final Integer deletionJobInterval = 24 * 60; // 1 day
    final Integer retention = 3 * 24 * 60; // 3 days
    final List<String> mpcs = Arrays.asList(new String[]{mpc1, mpc2});

    List<UserMessageDeletionJobEntity> currentDeletionJobs = null;

    @Before
    public void init() {
        currentDeletionJobs = new ArrayList<>();
    }

    @Test
    public void testDeletionStrategy() {
        Assert.assertTrue(messageRetentionService.handlesDeletionStrategy(DeletionStrategy.STORED_PROCEDURE.name()));
    }

    @Test
    public void testDeleteExpiredMessages() {
        new Expectations(messageRetentionService) {{
            pModeProvider.getMpcURIList();
            result = mpcs;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
            result = expiredDownloadedMessagesLimit;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
            result = expiredNotDownloadedMessagesLimit;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
            result = expiredSentMessagesLimit;
        }};

        messageRetentionService.deleteExpiredMessages();
    }

    @Test
    public void testAddDeletionJobsToList() {
        new Expectations(messageRetentionService) {{
            messageRetentionService.getRetention(mpc1, MessageStatus.ACKNOWLEDGED);
            result = 3 * 24 * 60;

            messageRetentionService.getMaxCount(MessageStatus.ACKNOWLEDGED);
            result = 5000;

            messageRetentionService.getProcedureName(MessageStatus.ACKNOWLEDGED);
            result = "DeleteExpiredSentMessages";

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_STORED_PROCEDURE_PARALLELDELETIONJOBSNO);
            result = 2;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_STORED_PROCEDURE_DELETIONJOBINTERVAL);
            result = 24* 60;
        }};

        List<UserMessageDeletionJobEntity> deletionJobs = new ArrayList<>();

        deletionJobs = messageRetentionService.addDeletionJobsToList(deletionJobs, mpc1, MessageStatus.ACKNOWLEDGED);
        Assert.assertTrue(deletionJobs.size() == 2);
        Assert.assertTrue(UserMessageDeletionJobState.NEW == UserMessageDeletionJobState.valueOf(deletionJobs.get(0).getState()));

        Assert.assertTrue(deletionJobs.get(1).getEndRetentionDate().before(deletionJobs.get(0).getStartRetentionDate()));
    }

    @Test
    public void testCancelAndCleanExpiredJobs() {
        new Expectations(messageRetentionService) {{
        }};

        messageRetentionService.cancelAndCleanExpiredJobs(currentDeletionJobs);
    }
}
